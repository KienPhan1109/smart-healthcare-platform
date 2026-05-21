package com.ptit.smart_healthcare_platform.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "lab_tests")
@Getter
@Setter
public class LabTest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Tên loại xét nghiệm (VD: Xét nghiệm máu toàn bộ CBC)
    @Column(unique = true, nullable = false, length = 200)
    private String name;

    // Đơn giá xét nghiệm
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price = BigDecimal.ZERO;

    // Mô tả chi tiết về loại xét nghiệm
    @Column(columnDefinition = "TEXT")
    private String description;

    // Phân loại phòng xét nghiệm: HEMATOLOGY, ULTRASOUND, IMAGING
    @Column(nullable = false, length = 50)
    private String roomType;

    @Column(name = "created_by", length = 50)
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_by", length = 50)
    private String updatedBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
