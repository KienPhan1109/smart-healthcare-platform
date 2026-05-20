package com.ptit.smart_healthcare_platform.model.dto.doctor;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@Data
public class PrescriptionItemDto {
    @NotNull(message = "Vui lòng chọn thuốc")
    private Long medicineId;

    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 1, message = "Số lượng phải lớn hơn 0")
    private Integer quantity;

    @NotBlank(message = "Hướng dẫn sử dụng không được để trống")
    private String instruction;
}
