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

    // Tên loại xét nghiệm (VD: Sinh thiết gan, Siêu âm ổ bụng)
    @Column(nullable = false, unique = true, length = 150)
    private String name;

    // Mô tả chi tiết mục đích xét nghiệm
    @Column(columnDefinition = "TEXT")
    private String description;

    // Đơn giá xét nghiệm
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price = BigDecimal.ZERO;

    // Trạng thái khả dụng (có thể tạm ngừng cung cấp nếu hỏng máy)
    @Column(name = "is_available", nullable = false)
    private Boolean isAvailable = true;

    @Column(name = "created_by", length = 50)
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_by", length = 50)
    private String updatedBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
