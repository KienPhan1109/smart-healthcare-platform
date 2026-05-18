package com.ptit.smart_healthcare_platform.model.dto.medicine;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class MedicineResponseDto {
    private Long id;
    private String name;
    private String unit;
    private BigDecimal price;
    private Integer stockQuantity;
    private String usageInstruction;
    private Boolean isActive;
}
