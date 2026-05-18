package com.ptit.smart_healthcare_platform.model.dto.medical;

import com.ptit.smart_healthcare_platform.model.dto.prescription.PrescriptionResponseDto;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class MedicalRecordResponseDto {
    private Long id;

    // CORE-07: Dữ liệu liên kết phức tạp (JOIN) - Tên bác sĩ
    private Long doctorId;
    private String doctorName;
    private String specialtyName;

    private Long patientId;
    private String patientName;

    private String symptoms;
    private String diagnosis;
    private String notes;
    private LocalDateTime createdAt;

    // CORE-07: Chi tiết danh sách thuốc đã kê
    private PrescriptionResponseDto prescription;
}
