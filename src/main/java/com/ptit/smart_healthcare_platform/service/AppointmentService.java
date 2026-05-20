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
import com.ptit.smart_healthcare_platform.repository.AppointmentRepository;
import com.ptit.smart_healthcare_platform.repository.DoctorRepository;
import com.ptit.smart_healthcare_platform.repository.PaymentRepository;
import com.ptit.smart_healthcare_platform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final PaymentRepository paymentRepository;
    private final PatientProfileService patientProfileService;
    private final UserRepository userRepository;

    @Transactional
    public Appointment bookAppointment(AppointmentBookingRequestDto dto, Long userId, String actor) {
        // 1. Verify patient ownership
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

        // 3. Verify future time
        if (dto.getAppointmentTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Thời gian khám phải ở trong tương lai");
        }

        // 4. Verify slot conflict
        boolean isConflict = appointmentRepository.existsByDoctorIdAndAppointmentTimeAndIsDeletedFalse(
                doctor.getId(), dto.getAppointmentTime());
        
        if (isConflict) {
            throw new IllegalStateException("Bác sĩ đã có lịch hẹn vào khung giờ này, vui lòng chọn thời gian khác.");
        }

        User createdByUser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));

        // 5. Create Appointment
        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setCreatedByUser(createdByUser);
        appointment.setAppointmentTime(dto.getAppointmentTime());
        appointment.setStatus(AppointmentStatus.PENDING);
        appointment.setSymptoms(dto.getSymptoms());
        appointment.setIsDeleted(false);
        appointment.setCreatedBy(actor);
        appointment.setCreatedAt(LocalDateTime.now());
        
        appointment = appointmentRepository.save(appointment);

        // 6. Create Payment
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
    public List<Appointment> getBookingHistory(Long userId) {
        return appointmentRepository.findAllByPatientUserIdAndIsDeletedFalseOrderByAppointmentTimeDesc(userId);
    }

    @Transactional
    public void cancelAppointment(Long id, String reason, Long userId, String actor) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lịch hẹn"));

        if (!appointment.getPatient().getUser().getId().equals(userId)) {
            throw new SecurityException("Bạn không có quyền hủy lịch hẹn này");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.setCancelReason(reason);
        appointment.setIsDeleted(true); // Release the slot
        appointment.setUpdatedBy(actor);
        appointment.setUpdatedAt(LocalDateTime.now());
        appointmentRepository.save(appointment);

        // Cancel pending payment if exists
        paymentRepository.findByAppointmentIdAndTypeAndIsDeletedFalse(id, PaymentType.EXAM_FEE)
                .ifPresent(payment -> {
                    if (payment.getStatus() == PaymentStatus.PENDING) {
                        payment.setStatus(PaymentStatus.CANCELLED);
                        payment.setUpdatedBy(actor);
                        payment.setUpdatedAt(LocalDateTime.now());
                        paymentRepository.save(payment);
                    }
                });
    }
}
