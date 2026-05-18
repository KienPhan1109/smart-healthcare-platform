package com.ptit.smart_healthcare_platform.model.dto.medical;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MedicinePrescriptionRequestDto {

    @NotNull(message = "Mã thuốc không được để trống")
    private Long medicineId;

    @NotNull(message = "Số lượng không được để trống")
    @Positive(message = "Số lượng phải lớn hơn 0")
    private Integer quantity;

    @NotBlank(message = "Liều dùng không được để trống")
    private String dosage;

    @NotBlank(message = "Tần suất không được để trống")
    private String frequency;

    private String note;
}
