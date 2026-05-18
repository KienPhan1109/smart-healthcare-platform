package com.ptit.smart_healthcare_platform.model.dto.medicine;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class MedicineRequestDto {

    @NotBlank(message = "Tên thuốc không được để trống")
    @Size(max = 200, message = "Tên thuốc không được vượt quá 200 ký tự")
    private String name;

    @NotBlank(message = "Đơn vị tính không được để trống")
    @Size(max = 50, message = "Đơn vị tính không được vượt quá 50 ký tự")
    private String unit;

    @NotNull(message = "Đơn giá không được để trống")
    @PositiveOrZero(message = "Đơn giá phải >= 0")
    private BigDecimal price;

    @NotNull(message = "Số lượng tồn kho không được để trống")
    @PositiveOrZero(message = "Số lượng tồn kho phải >= 0")
    private Integer stockQuantity;

    private String usageInstruction;
}
