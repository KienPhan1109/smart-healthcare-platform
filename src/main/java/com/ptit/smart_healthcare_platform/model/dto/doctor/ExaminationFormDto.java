package com.ptit.smart_healthcare_platform.model.dto.doctor;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.Valid;
import java.util.List;

@Data
public class ExaminationFormDto {
    @NotBlank(message = "Chẩn đoán không được để trống")
    private String diagnosis;
    
    private String note;

    @Valid
    private List<PrescriptionItemDto> prescriptionItems;
}
