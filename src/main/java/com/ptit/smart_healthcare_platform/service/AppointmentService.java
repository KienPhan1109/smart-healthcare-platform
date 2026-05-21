package com.ptit.smart_healthcare_platform.service;

import com.ptit.smart_healthcare_platform.model.dto.appointment.AppointmentBookingRequestDto;
import com.ptit.smart_healthcare_platform.model.entity.Appointment;
import com.ptit.smart_healthcare_platform.model.entity.Doctor;
import com.ptit.smart_healthcare_platform.model.entity.Patient;
import com.ptit.smart_healthcare_platform.model.entity.Payment;
import com.ptit.smart_healthcare_platform.model.entity.User;
import com.ptit.smart_healthcare_platform.model.enums.AppointmentStatus;
import com.ptit.smart_healthcare_platform.model.enums.DoctorStatus;
import com.ptit.smart_healthcare_platform.model.enums.PaymentStatus;
import com.ptit.smart_healthcare_platform.model.enums.PaymentType;
import com.ptit.smart_healthcare_platform.model.enums.TimeSlot;
import com.ptit.smart_healthcare_platform.repository.AppointmentRepository;
import com.ptit.smart_healthcare_platform.repository.DoctorRepository;
import com.ptit.smart_healthcare_platform.repository.PaymentRepository;
import com.ptit.smart_healthcare_platform.repository.UserRepository;
import com.ptit.smart_healthcare_platform.repository.LabOrderRepository;
import com.ptit.smart_healthcare_platform.repository.MedicalRecordRepository;
import com.ptit.smart_healthcare_platform.model.entity.MedicalRecord;
import com.ptit.smart_healthcare_platform.model.entity.Prescription;
import com.ptit.smart_healthcare_platform.model.entity.PrescriptionDetail;
import com.ptit.smart_healthcare_platform.model.entity.PrescriptionDetail;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final PaymentRepository paymentRepository;
    private final PatientProfileService patientProfileService;
    private final UserRepository userRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final LabOrderRepository labOrderRepository;

    /** Số ngày tối đa cho phép đặt lịch trước */
    private static final int MAX_BOOKING_DAYS_AHEAD = 7;

    /**
     * Trả về danh sách các TimeSlot khả dụng cho một bác sĩ trong một ngày cụ thể.
     * Loại bỏ: slot đã có lịch hẹn chưa hủy, slot đã quá giờ hiện tại (nếu ngày = hôm nay).
     */
    @Transactional(readOnly = true)
    public List<TimeSlot> getAvailableSlots(Long doctorId, LocalDate date) {
        // 1. Lấy tất cả lịch hẹn đã đặt của bác sĩ trong ngày
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
        List<Appointment> bookedAppointments = appointmentRepository
                .findAllByDoctorIdAndAppointmentTimeBetweenAndIsDeletedFalse(doctorId, startOfDay, endOfDay);

        // 2. Thu thập các giờ đã bị chiếm
        Set<LocalTime> bookedTimes = bookedAppointments.stream()
                .map(appt -> appt.getAppointmentTime().toLocalTime())
                .collect(Collectors.toSet());

        // 3. Lọc các slot khả dụng
        LocalDateTime now = LocalDateTime.now();
        List<TimeSlot> availableSlots = new ArrayList<>();

        for (TimeSlot slot : TimeSlot.values()) {
            LocalDateTime slotDateTime = slot.toLocalDateTime(date);

            // Bỏ qua slot đã quá giờ hiện tại
            if (slotDateTime.isBefore(now)) {
                continue;
            }

            // Bỏ qua slot đã có người đặt
            if (bookedTimes.contains(LocalTime.of(slot.getHour(), slot.getMinute()))) {
                continue;
            }

            availableSlots.add(slot);
        }

        return availableSlots;
    }

    @Transactional
    public Appointment bookAppointment(AppointmentBookingRequestDto dto, Long userId, String actor) {
        // 1. Verify patient ownership và tính hợp lệ hồ sơ
        if (dto.getPatientId() == null) {
            throw new IllegalArgumentException("Vui lòng chọn hồ sơ bệnh nhân");
        }
        Patient patient = patientProfileService.getProfileByIdAndUser(dto.getPatientId(), userId);
        if (patient.getFullName() == null || patient.getFullName().isBlank() ||
            patient.getDateOfBirth() == null ||
            patient.getGender() == null) {
            throw new IllegalArgumentException("Hồ sơ bệnh nhân chưa hợp lệ (yêu cầu đầy đủ Họ và tên, Ngày sinh, Giới tính)");
        }

        // 2. Verify doctor status
        Doctor doctor = doctorRepository.findById(dto.getDoctorId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bác sĩ"));
        
        if (doctor.getStatus() != DoctorStatus.ACTIVE) {
            throw new IllegalArgumentException("Bác sĩ hiện không nhận bệnh nhân");
        }

        // 3. Parse và validate ngày khám
        LocalDate appointmentDate;
        try {
            appointmentDate = LocalDate.parse(dto.getAppointmentDate());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Định dạng ngày khám không hợp lệ");
        }

        LocalDate today = LocalDate.now();
        if (appointmentDate.isBefore(today)) {
            throw new IllegalArgumentException("Ngày khám phải từ hôm nay trở đi");
        }
        if (appointmentDate.isAfter(today.plusDays(MAX_BOOKING_DAYS_AHEAD))) {
            throw new IllegalArgumentException("Chỉ được đặt lịch tối đa " + MAX_BOOKING_DAYS_AHEAD + " ngày tới");
        }

        // 4. Parse và validate slot giờ
        TimeSlot timeSlot;
        try {
            timeSlot = TimeSlot.valueOf(dto.getTimeSlot());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Khung giờ khám không hợp lệ");
        }

        LocalDateTime appointmentTime = timeSlot.toLocalDateTime(appointmentDate);

        // 5. Verify thời gian ở tương lai
        if (appointmentTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Khung giờ khám đã qua, vui lòng chọn khung giờ khác");
        }

        // 6. Verify slot chưa bị chiếm
        boolean isConflict = appointmentRepository.existsByDoctorIdAndAppointmentTimeAndIsDeletedFalse(
                doctor.getId(), appointmentTime);
        
        if (isConflict) {
            throw new IllegalStateException("Bác sĩ đã có lịch hẹn vào khung giờ này, vui lòng chọn thời gian khác.");
        }

        User createdByUser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));

        // 7. Create Appointment
        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setCreatedByUser(createdByUser);
        appointment.setAppointmentTime(appointmentTime);
        appointment.setStatus(AppointmentStatus.PENDING);
        appointment.setSymptoms(dto.getSymptoms());
        appointment.setIsDeleted(false);
        appointment.setCreatedBy(actor);
        appointment.setCreatedAt(LocalDateTime.now());
        
        appointment = appointmentRepository.save(appointment);

        // 8. Create Payment
        Payment payment = new Payment();
        payment.setPatient(patient);
        payment.setAppointment(appointment);
        payment.setType(PaymentType.EXAM_FEE);
        payment.setAmount(doctor.getExamFee());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setIsDeleted(false);
        payment.setCreatedBy(actor);
        payment.setCreatedAt(LocalDateTime.now());
        
        paymentRepository.save(payment);

        return appointment;
    }

    @Transactional(readOnly = true)
    public List<Appointment> getBookingHistory(Long userId, String statusFilter) {
        List<Appointment> allAppointments = appointmentRepository.findAllByPatientUserIdOrderByAppointmentTimeDesc(userId);
        allAppointments.forEach(appt -> appt.getPayments().size()); // Initialize lazy collection

        if (statusFilter == null || statusFilter.isBlank() || "ALL".equalsIgnoreCase(statusFilter)) {
            return allAppointments;
        }

        return allAppointments.stream().filter(appt -> {
            boolean hasPendingLabOrder = false;
            if (appt.getStatus() == AppointmentStatus.WAITING_FOR_LAB) {
                hasPendingLabOrder = labOrderRepository.findByAppointmentIdAndIsDeletedFalse(appt.getId())
                        .map(order -> "PENDING".equals(order.getStatus()))
                        .orElse(false);
            }

            switch (statusFilter.toUpperCase()) {
                case "UPCOMING":
                    return appt.getStatus() == AppointmentStatus.CONFIRMED;
                case "IN_PROGRESS":
                    return appt.getStatus() == AppointmentStatus.EXAMINING
                        || appt.getStatus() == AppointmentStatus.READY_FOR_REEXAM
                        || (appt.getStatus() == AppointmentStatus.WAITING_FOR_LAB && !hasPendingLabOrder);
                case "UNPAID":
                    return appt.getStatus() == AppointmentStatus.PENDING 
                        || appt.getStatus() == AppointmentStatus.WAITING_FOR_DRUG_PAYMENT
                        || (appt.getStatus() == AppointmentStatus.WAITING_FOR_LAB && hasPendingLabOrder);
                case "COMPLETED":
                    return appt.getStatus() == AppointmentStatus.COMPLETED;
                case "CANCELLED":
                    return appt.getStatus() == AppointmentStatus.CANCELLED;
                default:
                    return true;
            }
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Appointment getAppointmentByIdAndUser(Long id, Long userId) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lịch hẹn"));
        if (!appointment.getPatient().getUser().getId().equals(userId)) {
            throw new SecurityException("Bạn không có quyền truy cập lịch hẹn này");
        }
        
        // Khởi tạo trước các liên kết lazy để tránh LazyInitializationException ngoài Transaction
        if (appointment.getDoctor() != null) {
            appointment.getDoctor().getAcademicRank();
            if (appointment.getDoctor().getUser() != null) {
                appointment.getDoctor().getUser().getFullName();
                appointment.getDoctor().getUser().getPhoneNumber();
            }
            if (appointment.getDoctor().getSpecialty() != null) {
                appointment.getDoctor().getSpecialty().getName();
            }
        }
        if (appointment.getPatient() != null) {
            appointment.getPatient().getFullName();
            if (appointment.getPatient().getUser() != null) {
                appointment.getPatient().getUser().getId();
            }
        }
        
        return appointment;
    }

    @Transactional(readOnly = true)
    public MedicalRecord getMedicalRecordWithPrescriptionByAppointmentId(Long appointmentId) {
        MedicalRecord record = medicalRecordRepository.findByAppointmentId(appointmentId).orElse(null);
        if (record != null) {
            // Khởi tạo thông tin Doctor
            if (record.getDoctor() != null) {
                record.getDoctor().getAcademicRank();
                if (record.getDoctor().getUser() != null) {
                    record.getDoctor().getUser().getFullName();
                }
            }
            // Khởi tạo thông tin Patient
            if (record.getPatient() != null) {
                record.getPatient().getFullName();
            }
            // Khởi tạo thông tin Prescription cùng danh sách chi tiết và thông tin Medicine đi kèm
            if (record.getPrescription() != null) {
                record.getPrescription().getOriginalAmount();
                if (record.getPrescription().getPrescriptionDetails() != null) {
                    record.getPrescription().getPrescriptionDetails().size();
                    for (PrescriptionDetail detail : record.getPrescription().getPrescriptionDetails()) {
                        detail.getQuantity();
                        if (detail.getMedicine() != null) {
                            detail.getMedicine().getName();
                        }
                    }
                }
            }
        }
        return record;
    }

    @Transactional
    public void cancelAppointment(Long id, String reason, Long userId, String actor) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lịch hẹn"));

        if (!appointment.getPatient().getUser().getId().equals(userId)) {
            throw new SecurityException("Bạn không có quyền hủy lịch hẹn này");
        }

        boolean eligibleForRefund = Duration.between(LocalDateTime.now(), appointment.getAppointmentTime()).toHours() >= 24;
        String prefix = eligibleForRefund ? "[Hủy trước 24h - Có hoàn tiền] " : "[Hủy trễ dưới 24h - Không hoàn tiền] ";

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.setCancelReason(prefix + reason);
        appointment.setIsDeleted(true); // Release the slot
        appointment.setUpdatedBy(actor);
        appointment.setUpdatedAt(LocalDateTime.now());
        appointmentRepository.save(appointment);

        // Cancel pending payment if exists, or refund if paid
        paymentRepository.findByAppointmentIdAndTypeAndIsDeletedFalse(id, PaymentType.EXAM_FEE)
                .ifPresent(payment -> {
                    if (payment.getStatus() == PaymentStatus.PENDING) {
                        payment.setStatus(PaymentStatus.CANCELLED);
                        payment.setUpdatedBy(actor);
                        payment.setUpdatedAt(LocalDateTime.now());
                        paymentRepository.save(payment);
                    } else if (payment.getStatus() == PaymentStatus.PAID) {
                        if (eligibleForRefund) {
                            payment.setStatus(PaymentStatus.REFUNDED);
                            payment.setUpdatedBy(actor);
                            payment.setUpdatedAt(LocalDateTime.now());
                            paymentRepository.save(payment);
                        }
                    }
                });
    }
}
