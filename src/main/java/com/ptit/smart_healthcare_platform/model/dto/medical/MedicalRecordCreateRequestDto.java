package com.ptit.smart_healthcare_platform.model.dto.medical;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MedicalRecordCreateRequestDto {

    @NotNull(message = "Mã lịch hẹn không được để trống")
    private Long appointmentId;

    // Triệu chứng bác sĩ ghi nhận khi khám trực tiếp
    @NotBlank(message = "Triệu chứng không được để trống")
    private String symptoms;

    @NotBlank(message = "Chẩn đoán không được để trống")
    private String diagnosis;

    private String notes;

    // CORE-06: Danh sách thuốc kê đơn (Transaction - lưu nhiều bảng cùng lúc)
    @Valid
    private List<MedicinePrescriptionRequestDto> medicines;

    // Ghi chú cho đơn thuốc
    private String prescriptionNotes;
}
