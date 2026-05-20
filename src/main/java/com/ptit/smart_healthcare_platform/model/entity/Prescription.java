package com.ptit.smart_healthcare_platform.model.entity;

import com.ptit.smart_healthcare_platform.model.enums.PrescriptionStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "prescriptions")
@Getter
@Setter
public class Prescription {
    @Id
    private Long id;

    // Liên kết 1-1 với bệnh án (một bệnh án chỉ có một đơn thuốc)
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "medical_record_id")
    private MedicalRecord medicalRecord;

    // Tỷ lệ chiết khấu BHYT mặc định theo luật Việt Nam (80%)
    public static final java.math.BigDecimal INSURANCE_DISCOUNT_RATE = new java.math.BigDecimal("0.80");

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PrescriptionStatus status = PrescriptionStatus.PENDING;

    // Đánh dấu đơn thuốc có áp dụng BHYT
    @Column(name = "is_insurance_applied", nullable = false)
    private Boolean isInsuranceApplied = false;

    // Tổng tiền thuốc gốc trước chiết khấu
    @Column(name = "original_amount", nullable = false, precision = 12, scale = 2)
    private java.math.BigDecimal originalAmount = java.math.BigDecimal.ZERO;

    // Số tiền được BHYT hỗ trợ chi trả (80% của originalAmount)
    @Column(name = "discount_amount", nullable = false, precision = 12, scale = 2)
    private java.math.BigDecimal discountAmount = java.math.BigDecimal.ZERO;

    // Số tiền thực tế bệnh nhân tự chi trả
    @Column(name = "final_amount", nullable = false, precision = 12, scale = 2)
    private java.math.BigDecimal finalAmount = java.math.BigDecimal.ZERO;

    // Ghi chú của bác sĩ cho đơn thuốc
    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(nullable = false, updatable = false)
    private LocalDateTime issuedDate = LocalDateTime.now();



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

    // Danh sách chi tiết thuốc trong đơn
    @OneToMany(mappedBy = "prescription", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PrescriptionDetail> prescriptionDetails = new ArrayList<>();
}
