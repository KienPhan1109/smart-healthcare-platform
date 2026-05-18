package com.ptit.smart_healthcare_platform.model.dto.patient;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class PatientProfileResponseDto {
    private Long patientId;
    private String fullName;
    private LocalDate dateOfBirth;
    private String gender;
    private String phoneNumber;

    // Dữ liệu y tế - chỉ trả về cho chính bệnh nhân hoặc bác sĩ đang khám
    private String medicalHistory;
    private String allergies;
    private String bloodType;
    private BigDecimal height;
    private BigDecimal weight;
}
