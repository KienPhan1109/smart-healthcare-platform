package com.ptit.smart_healthcare_platform.model.dto.prescription;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class PrescriptionResponseDto {
    private Long id;
    private String status;
    private String notes;
    private LocalDateTime issuedDate;

    // Thông tin dược sĩ cấp phát (nếu đã cấp phát)
    private String dispensedByName;
    private LocalDateTime dispensedAt;

    private List<PrescriptionItemResponseDto> items;
}
