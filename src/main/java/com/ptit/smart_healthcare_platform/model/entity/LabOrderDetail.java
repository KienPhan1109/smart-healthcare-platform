package com.ptit.smart_healthcare_platform.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "lab_order_details")
@Getter
@Setter
public class LabOrderDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Thuộc phiếu chỉ định nào
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lab_order_id", nullable = false)
    private LabOrder labOrder;

    // Loại xét nghiệm được chỉ định
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "lab_test_id", nullable = false)
    private LabTest labTest;

    // Đơn giá tại thời điểm chỉ định (snapshot giá)
    @Column(name = "price_at_order", nullable = false, precision = 12, scale = 2)
    private BigDecimal priceAtOrder = BigDecimal.ZERO;

    // Kết quả xét nghiệm do Kỹ thuật viên nhập (văn bản thủ công)
    @Column(columnDefinition = "TEXT")
    private String result;

    // Thời điểm Kỹ thuật viên trả kết quả
    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "created_by", length = 50)
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_by", length = 50)
    private String updatedBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
