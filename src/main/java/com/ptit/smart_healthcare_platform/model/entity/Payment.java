package com.ptit.smart_healthcare_platform.model.entity;

import com.ptit.smart_healthcare_platform.model.enums.PaymentStatus;
import com.ptit.smart_healthcare_platform.model.enums.PaymentType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Bệnh nhân thực hiện thanh toán
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    // Nguồn gốc ca bệnh (Một lịch hẹn có thể có nhiều payment: phí khám, phí xét nghiệm, phí thuốc)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id", nullable = false)
    private Appointment appointment;

    // Loại thanh toán
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentType type;

    // Số tiền thực tế phải nộp (đã trừ bảo hiểm nếu có)
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount = BigDecimal.ZERO;

    // Phương thức thanh toán (CASH, VNPAY, MOMO)
    @Column(name = "payment_method", length = 20)
    private String paymentMethod;

    // Mã giao dịch từ cổng thanh toán điện tử
    @Column(name = "transaction_id", length = 100)
    private String transactionId;

    // Trạng thái thanh toán
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status = PaymentStatus.PENDING;

    // Thời điểm thanh toán thành công
    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    // Thu ngân xác nhận thu tiền (nếu là tiền mặt)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cashier_id")
    private User cashier;

    // Xóa mềm hóa đơn (trong trường hợp hủy sai lệch)
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "created_by", length = 50)
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_by", length = 50)
    private String updatedBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
