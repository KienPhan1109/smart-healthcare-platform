package com.ptit.smart_healthcare_platform.model.dto.appointment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AppointmentBookingRequestDto {

    @NotNull(message = "Mã bác sĩ không được để trống")
    private Long doctorId;

    // Dành cho Điều phối viên đặt lịch hộ bệnh nhân hoặc bệnh nhân tự chọn
    @NotNull(message = "Vui lòng chọn hồ sơ bệnh nhân")
    private Long patientId;

    // Ngày khám (format yyyy-MM-dd từ input type="date")
    @NotBlank(message = "Ngày khám không được để trống")
    private String appointmentDate;

    // Khung giờ khám (tên enum TimeSlot, ví dụ: SLOT_08_00)
    @NotBlank(message = "Khung giờ khám không được để trống")
    private String timeSlot;

    @NotBlank(message = "Mô tả triệu chứng hoặc lý do khám không được để trống")
    private String symptoms;
}
