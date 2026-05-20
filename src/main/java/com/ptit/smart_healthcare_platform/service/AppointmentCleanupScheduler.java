package com.ptit.smart_healthcare_platform.service;

import com.ptit.smart_healthcare_platform.model.entity.Appointment;
import com.ptit.smart_healthcare_platform.model.enums.AppointmentStatus;
import com.ptit.smart_healthcare_platform.model.enums.PaymentStatus;
import com.ptit.smart_healthcare_platform.model.enums.PaymentType;
import com.ptit.smart_healthcare_platform.repository.AppointmentRepository;
import com.ptit.smart_healthcare_platform.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AppointmentCleanupScheduler {

    private final AppointmentRepository appointmentRepository;
    private final PaymentRepository paymentRepository;

    @Scheduled(fixedRate = 30000) // Every 30 seconds
    @Transactional
    public void cleanupExpiredAppointments() {
        LocalDateTime timeLimit = LocalDateTime.now().minusMinutes(3);
        List<Appointment> expiredAppointments = appointmentRepository.findAllByStatusAndCreatedAtBeforeAndIsDeletedFalse(
                AppointmentStatus.PENDING, timeLimit);

        for (Appointment appt : expiredAppointments) {
            // Check if there is a paid payment
            boolean isPaid = paymentRepository.findByAppointmentIdAndTypeAndIsDeletedFalse(appt.getId(), PaymentType.EXAM_FEE)
                    .map(payment -> payment.getStatus() == PaymentStatus.PAID)
                    .orElse(false);

            if (!isPaid) {
                log.info("Cancelling expired appointment ID: {}", appt.getId());
                
                appt.setStatus(AppointmentStatus.CANCELLED);
                appt.setCancelReason("Quá thời gian 3 phút giữ chỗ thanh toán");
                appt.setIsDeleted(true); // Release slot
                appt.setUpdatedBy("SYSTEM");
                appt.setUpdatedAt(LocalDateTime.now());
                appointmentRepository.save(appt);

                // Cancel the payment
                paymentRepository.findByAppointmentIdAndTypeAndIsDeletedFalse(appt.getId(), PaymentType.EXAM_FEE)
                        .ifPresent(payment -> {
                            payment.setStatus(PaymentStatus.CANCELLED);
                            payment.setUpdatedBy("SYSTEM");
                            payment.setUpdatedAt(LocalDateTime.now());
                            paymentRepository.save(payment);
                        });
            }
        }
    }
}
