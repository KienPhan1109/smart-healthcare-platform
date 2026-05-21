package com.ptit.smart_healthcare_platform.model.dto.technician;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LabResultRequestDto {
    @NotBlank(message = "Vui lòng nhập kết quả phân tích / chỉ số cận lâm sàng")
    private String result;
}
