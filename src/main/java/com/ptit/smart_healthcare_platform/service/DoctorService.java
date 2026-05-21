package com.ptit.smart_healthcare_platform.service;

import com.ptit.smart_healthcare_platform.model.dto.doctor.ExaminationFormDto;
import com.ptit.smart_healthcare_platform.model.dto.doctor.PrescriptionItemDto;
import com.ptit.smart_healthcare_platform.model.entity.*;
import com.ptit.smart_healthcare_platform.model.enums.AppointmentStatus;
import com.ptit.smart_healthcare_platform.model.enums.PaymentStatus;
import com.ptit.smart_healthcare_platform.model.enums.PaymentType;
import com.ptit.smart_healthcare_platform.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DoctorService {

    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final PrescriptionDetailRepository prescriptionDetailRepository;
    private final MedicineRepository medicineRepository;
    private final PaymentRepository paymentRepository;
    private final LabTestRepository labTestRepository;
    private final LabOrderRepository labOrderRepository;
    private final LabOrderDetailRepository labOrderDetailRepository;

    @Transactional(readOnly = true)
    public Doctor getDoctorProfile(Long userId) {
        return doctorRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hồ sơ bác sĩ"));
    }

    @Transactional(readOnly = true)
    public List<Appointment> getTodayAppointments(Long doctorId) {
        // Lấy tất cả lịch khám của bác sĩ (loại trừ đã hủy và chưa thanh toán phí khám) không phân biệt ngày để tiện test
        return appointmentRepository.findAllByDoctorIdAndIsDeletedFalseOrderByAppointmentTimeAsc(doctorId).stream()
                .filter(a -> a.getStatus() != AppointmentStatus.PENDING && a.getStatus() != AppointmentStatus.CANCELLED)
                .collect(Collectors.toList());
    }

    @Transactional
    public void startExamining(Long appointmentId, Long doctorId) {
        Appointment appt = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lịch hẹn"));
        
        if (!appt.getDoctor().getId().equals(doctorId)) {
            throw new SecurityException("Bạn không có quyền thao tác trên lịch hẹn này");
        }

        if (appt.getStatus() == AppointmentStatus.CONFIRMED) {
            appt.setStatus(AppointmentStatus.EXAMINING);
            appt.setUpdatedAt(LocalDateTime.now());
            appointmentRepository.save(appt);
        } else if (appt.getStatus() == AppointmentStatus.READY_FOR_REEXAM) {
            // Tái khám ưu tiên: chuyển lại sang EXAMINING
            appt.setStatus(AppointmentStatus.EXAMINING);
            appt.setUpdatedAt(LocalDateTime.now());
            appointmentRepository.save(appt);
        }
    }

    @Transactional
    public void submitExamination(Long appointmentId, Long doctorId, ExaminationFormDto form, String actor) {
        Appointment appt = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lịch hẹn"));

        if (!appt.getDoctor().getId().equals(doctorId)) {
            throw new SecurityException("Bạn không có quyền thao tác trên lịch hẹn này");
        }

        if (appt.getStatus() != AppointmentStatus.EXAMINING) {
            throw new IllegalStateException("Trạng thái lịch hẹn không hợp lệ để hoàn tất khám (cần ở trạng thái Đang khám)");
        }

        // 1. Tạo MedicalRecord (nếu chưa có - trường hợp tái khám có thể đã có record sơ bộ)
        MedicalRecord record = medicalRecordRepository.findByAppointmentId(appointmentId).orElse(null);
        if (record == null) {
            record = new MedicalRecord();
            record.setAppointment(appt);
            record.setPatient(appt.getPatient());
            record.setDoctor(appt.getDoctor());
            record.setSymptoms(appt.getSymptoms() != null ? appt.getSymptoms() : "");
            record.setCreatedBy(actor);
            record.setCreatedAt(LocalDateTime.now());
        }
        record.setDiagnosis(form.getDiagnosis());
        record.setNotes(form.getNote());
        record.setUpdatedBy(actor);
        record.setUpdatedAt(LocalDateTime.now());
        record = medicalRecordRepository.save(record);

        // 2. Nếu có kê đơn thuốc, tạo Prescription và chi tiết
        BigDecimal totalDrugPrice = BigDecimal.ZERO;

        if (form.getPrescriptionItems() != null && !form.getPrescriptionItems().isEmpty()) {
            Prescription prescription = new Prescription();
            prescription.setMedicalRecord(record);
            prescription.setNotes(form.getNote());
            prescription.setCreatedBy(actor);
            prescription.setCreatedAt(LocalDateTime.now());
            prescription = prescriptionRepository.save(prescription);

            for (PrescriptionItemDto item : form.getPrescriptionItems()) {
                Medicine medicine = medicineRepository.findById(item.getMedicineId())
                        .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thuốc ID: " + item.getMedicineId()));
                
                // --- Bổ sung xử lý Tồn Kho (Stock) ---
                if (medicine.getStockQuantity() < item.getQuantity()) {
                    throw new IllegalArgumentException("Thuốc '" + medicine.getName() + "' không đủ số lượng tồn kho (Còn lại: " + medicine.getStockQuantity() + ")");
                }
                
                // Trừ số lượng tồn kho và lưu lại
                medicine.setStockQuantity(medicine.getStockQuantity() - item.getQuantity());
                medicine.setUpdatedAt(LocalDateTime.now());
                medicine.setUpdatedBy(actor);
                medicineRepository.save(medicine);
                // ------------------------------------

                PrescriptionDetail detail = new PrescriptionDetail();
                detail.setPrescription(prescription);
                detail.setMedicine(medicine);
                detail.setQuantity(item.getQuantity());
                detail.setDosage("-"); // Không bắt buộc nhập liều lượng riêng trong form đơn giản
                detail.setFrequency(item.getInstruction());
                detail.setPriceAtPrescription(medicine.getPrice());
                detail.setUnitAtPrescription(medicine.getUnit());
                
                BigDecimal itemTotal = medicine.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                totalDrugPrice = totalDrugPrice.add(itemTotal);

                prescriptionDetailRepository.save(detail);
            }

            // Cập nhật tổng tiền đơn thuốc
            prescription.setOriginalAmount(totalDrugPrice);
            prescription.setFinalAmount(totalDrugPrice);
            prescriptionRepository.save(prescription);

            // 3. Tạo thanh toán tiền thuốc (nếu có đơn thuốc)
            Payment drugPayment = new Payment();
            drugPayment.setPatient(appt.getPatient());
            drugPayment.setAppointment(appt);
            drugPayment.setType(PaymentType.PHARMACY_FEE);
            drugPayment.setAmount(totalDrugPrice);
            drugPayment.setStatus(PaymentStatus.PENDING);
            drugPayment.setCreatedBy(actor);
            drugPayment.setCreatedAt(LocalDateTime.now());
            paymentRepository.save(drugPayment);
            
            appt.setStatus(AppointmentStatus.WAITING_FOR_DRUG_PAYMENT);
        } else {
            // Nếu không có đơn thuốc -> kết thúc luôn
            appt.setStatus(AppointmentStatus.COMPLETED);
        }

        appt.setUpdatedAt(LocalDateTime.now());
        appt.setUpdatedBy(actor);
        appointmentRepository.save(appt);
    }

    /**
     * Bác sĩ chỉ định xét nghiệm cận lâm sàng cho lịch hẹn.
     * Tạo LabOrder, LabOrderDetail, Payment (LAB_FEE) và chuyển trạng thái sang WAITING_FOR_LAB.
     */
    @Transactional
    public void submitLabOrder(Long appointmentId, Long doctorId, List<Long> labTestIds, String actor) {
        Appointment appt = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lịch hẹn"));

        if (!appt.getDoctor().getId().equals(doctorId)) {
            throw new SecurityException("Bạn không có quyền thao tác trên lịch hẹn này");
        }

        if (appt.getStatus() != AppointmentStatus.EXAMINING) {
            throw new IllegalStateException("Trạng thái lịch hẹn không hợp lệ để chỉ định xét nghiệm");
        }

        if (labTestIds == null || labTestIds.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn ít nhất một loại xét nghiệm");
        }

        // Tạo LabOrder
        LabOrder labOrder = new LabOrder();
        labOrder.setAppointment(appt);
        labOrder.setDoctor(appt.getDoctor());
        labOrder.setStatus("PENDING");
        labOrder.setCreatedBy(actor);
        labOrder.setCreatedAt(LocalDateTime.now());
        labOrder = labOrderRepository.save(labOrder);

        BigDecimal totalLabFee = BigDecimal.ZERO;

        for (Long labTestId : labTestIds) {
            LabTest labTest = labTestRepository.findById(labTestId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy loại xét nghiệm ID: " + labTestId));

            LabOrderDetail detail = new LabOrderDetail();
            detail.setLabOrder(labOrder);
            detail.setLabTest(labTest);
            detail.setPriceAtOrder(labTest.getPrice());
            detail.setCreatedBy(actor);
            detail.setCreatedAt(LocalDateTime.now());
            labOrderDetailRepository.save(detail);

            totalLabFee = totalLabFee.add(labTest.getPrice());
        }

        // Cập nhật tổng phí xét nghiệm
        labOrder.setTotalAmount(totalLabFee);
        labOrderRepository.save(labOrder);

        // Tạo Payment cho phí xét nghiệm (LAB_FEE)
        Payment labPayment = new Payment();
        labPayment.setPatient(appt.getPatient());
        labPayment.setAppointment(appt);
        labPayment.setType(PaymentType.LAB_FEE);
        labPayment.setAmount(totalLabFee);
        labPayment.setStatus(PaymentStatus.PENDING);
        labPayment.setCreatedBy(actor);
        labPayment.setCreatedAt(LocalDateTime.now());
        paymentRepository.save(labPayment);

        // Chuyển trạng thái lịch hẹn sang WAITING_FOR_LAB
        appt.setStatus(AppointmentStatus.WAITING_FOR_LAB);
        appt.setUpdatedAt(LocalDateTime.now());
        appt.setUpdatedBy(actor);
        appointmentRepository.save(appt);
    }

    /**
     * Lấy LabOrder của một lịch hẹn (nếu có).
     */
    @Transactional(readOnly = true)
    public LabOrder getLabOrderByAppointmentId(Long appointmentId) {
        return labOrderRepository.findByAppointmentIdAndIsDeletedFalse(appointmentId).orElse(null);
    }
}
