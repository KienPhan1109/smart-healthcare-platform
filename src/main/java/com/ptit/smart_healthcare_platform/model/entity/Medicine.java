package com.ptit.smart_healthcare_platform.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "medicines")
@Getter
@Setter
public class Medicine extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    // Đơn vị tính (VD: Viên, Ống, Chai, Gói)
    @Column(nullable = false, length = 50)
    private String unit;

    // Đơn giá (VNĐ)
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price = BigDecimal.ZERO;

    // Số lượng tồn kho hiện tại - phục vụ CORE-08 (trừ lùi khi cấp phát)
    @Column(nullable = false)
    private Integer stockQuantity = 0;

    // Hướng dẫn sử dụng mặc định
    @Column(columnDefinition = "TEXT")
    private String usageInstruction;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private com.ptit.smart_healthcare_platform.model.enums.MedicineStatus status = com.ptit.smart_healthcare_platform.model.enums.MedicineStatus.SELLING;
}

