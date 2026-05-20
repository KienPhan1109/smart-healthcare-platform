package com.ptit.smart_healthcare_platform.service;

import com.ptit.smart_healthcare_platform.model.entity.Appointment;
import com.ptit.smart_healthcare_platform.model.entity.Payment;
import com.ptit.smart_healthcare_platform.model.enums.AppointmentStatus;
import com.ptit.smart_healthcare_platform.model.enums.PaymentStatus;
import com.ptit.smart_healthcare_platform.model.enums.PaymentType;
import com.ptit.smart_healthcare_platform.repository.AppointmentRepository;
import com.ptit.smart_healthcare_platform.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentSimulationService {

    private final PaymentRepository paymentRepository;
    private final AppointmentRepository appointmentRepository;

    @Transactional(readOnly = true)
    public Payment getExamFeePayment(Long appointmentId, Long userId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lịch hẹn"));

        if (!appointment.getPatient().getUser().getId().equals(userId)) {
            throw new SecurityException("Bạn không có quyền xem thông tin thanh toán này");
        }

        return paymentRepository.findByAppointmentIdAndTypeAndIsDeletedFalse(appointmentId, PaymentType.EXAM_FEE)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy hóa đơn phí khám"));
    }

    @Transactional
    public void processSimulatedPayment(Long appointmentId, String method, Long userId, String actor) {
        Payment payment = getExamFeePayment(appointmentId, userId);
        Appointment appointment = payment.getAppointment();

        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new IllegalStateException("Lịch hẹn đã bị hủy, không thể thanh toán");
        }

        if (payment.getStatus() == PaymentStatus.PAID) {
            throw new IllegalStateException("Hóa đơn này đã được thanh toán");
        }

        // Check if expired
        LocalDateTime expiryTime = appointment.getCreatedAt().plusMinutes(3);
        if (LocalDateTime.now().isAfter(expiryTime)) {
            throw new IllegalStateException("Đã quá 3 phút giữ chỗ, lịch hẹn không còn hợp lệ");
        }

        // Update payment
        payment.setStatus(PaymentStatus.PAID);
        payment.setPaidAt(LocalDateTime.now());
        payment.setPaymentMethod(method);
        payment.setTransactionId("TXN-" + UUID.randomUUID().toString());
        payment.setUpdatedBy(actor);
        payment.setUpdatedAt(LocalDateTime.now());
        paymentRepository.save(payment);

        // Update appointment status to CONFIRMED
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        appointment.setUpdatedBy(actor);
        appointment.setUpdatedAt(LocalDateTime.now());
        appointmentRepository.save(appointment);
    }

    @Transactional(readOnly = true)
    public Payment getDrugFeePayment(Long appointmentId, Long userId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lịch hẹn"));

        if (!appointment.getPatient().getUser().getId().equals(userId)) {
            throw new SecurityException("Bạn không có quyền xem thông tin thanh toán này");
        }

        return paymentRepository.findByAppointmentIdAndTypeAndIsDeletedFalse(appointmentId, PaymentType.PHARMACY_FEE)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy hóa đơn tiền thuốc"));
    }

    @Transactional
    public void processSimulatedDrugPayment(Long appointmentId, String method, Long userId, String actor) {
        Payment payment = getDrugFeePayment(appointmentId, userId);
        Appointment appointment = payment.getAppointment();

        if (payment.getStatus() == PaymentStatus.PAID) {
            throw new IllegalStateException("Hóa đơn thuốc này đã được thanh toán");
        }

        if (appointment.getStatus() != AppointmentStatus.WAITING_FOR_DRUG_PAYMENT) {
            throw new IllegalStateException("Trạng thái lịch hẹn không hợp lệ để thanh toán thuốc");
        }

        // Update payment
        payment.setStatus(PaymentStatus.PAID);
        payment.setPaidAt(LocalDateTime.now());
        payment.setPaymentMethod(method);
        payment.setTransactionId("DRUG-" + UUID.randomUUID().toString());
        payment.setUpdatedBy(actor);
        payment.setUpdatedAt(LocalDateTime.now());
        paymentRepository.save(payment);

        // Update appointment status to COMPLETED
        appointment.setStatus(AppointmentStatus.COMPLETED);
        appointment.setUpdatedBy(actor);
        appointment.setUpdatedAt(LocalDateTime.now());
        appointmentRepository.save(appointment);
    }
}
