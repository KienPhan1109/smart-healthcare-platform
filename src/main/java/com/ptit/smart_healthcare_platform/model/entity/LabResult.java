package com.ptit.smart_healthcare_platform.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "lab_results")
@Getter
@Setter
public class LabResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Liên kết với Phiếu chỉ định gốc
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lab_order_id", nullable = false)
    private LabOrder labOrder;

    // Loại xét nghiệm được thực hiện
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lab_test_id", nullable = false)
    private LabTest labTest;

    // Chỉ số kết quả đo được (chỉ có khi KTV đã nhập)
    @Column(name = "result_value", columnDefinition = "TEXT")
    private String resultValue;

    // Khoảng tham chiếu sinh học (VD: 4.0 - 10.0 mmol/L)
    @Column(name = "reference_range", length = 100)
    private String referenceRange;

    // Đánh giá bình thường / bất thường
    @Column(name = "is_abnormal")
    private Boolean isAbnormal;

    // Kỹ thuật viên (User) thực hiện xét nghiệm
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "technician_id")
    private User technician;

    // Thời gian nhập kết quả
    @Column(name = "performed_at")
    private LocalDateTime performedAt;

    // Snapshot giá xét nghiệm tại thời điểm chỉ định
    @Column(name = "price_at_order", nullable = false, precision = 12, scale = 2)
    private BigDecimal priceAtOrder;

    @Column(name = "created_by", length = 50)
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_by", length = 50)
    private String updatedBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
