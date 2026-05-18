package com.ptit.smart_healthcare_platform.model.dto.prescription;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PrescriptionItemResponseDto {
    private Long id;
    private Long medicineId;
    private String medicineName;
    private String unit;
    private Integer quantity;
    private String dosage;
    private String frequency;
    private String note;
}
