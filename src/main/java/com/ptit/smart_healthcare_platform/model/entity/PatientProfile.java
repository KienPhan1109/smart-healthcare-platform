package com.ptit.smart_healthcare_platform.model.entity;

import com.ptit.smart_healthcare_platform.model.enums.BloodType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "patient_profiles")
@Getter
@Setter
public class PatientProfile extends BaseEntity {
    @Id
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "patient_id")
    private Patient patient;

    // Dữ liệu y tế nhạy cảm - chỉ trả về qua DTO có kiểm soát phân quyền
    @Column(columnDefinition = "TEXT")
    private String medicalHistory;

    @Column(columnDefinition = "TEXT")
    private String allergies;

    // Sử dụng Enum thay vì String tự do để đảm bảo tính chính xác dữ liệu y tế
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private BloodType bloodType = BloodType.UNKNOWN;

    // Chiều cao (cm) - precision=5, scale=2 cho phép giá trị tối đa 999.99
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal height = BigDecimal.ZERO;

    // Cân nặng (kg) - precision=5, scale=2 cho phép giá trị tối đa 999.99
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal weight = BigDecimal.ZERO;

    // Mã số thẻ BHYT duy nhất (Chuẩn 15 ký tự Việt Nam, VD: GD4797918800001)
    @Column(name = "insurance_number", unique = true, length = 15)
    private String insuranceNumber;

    // Ngày hết hạn thẻ BHYT phục vụ việc tự động kiểm soát hiệu lực khi cấp phát
    @Column(name = "insurance_expiry_date")
    private java.time.LocalDate insuranceExpiryDate;
}