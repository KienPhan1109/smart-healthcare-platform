package com.ptit.smart_healthcare_platform.model.dto.appointment;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class AppointmentBookingRequestDto {

    @NotNull(message = "Mã bác sĩ không được để trống")
    private Long doctorId;

    // Dành cho Điều phối viên đặt lịch hộ bệnh nhân
    private Long patientId;

    // CORE-05: Không cho phép đặt lịch ngược về quá khứ
    @NotNull(message = "Thời gian khám không được để trống")
    @Future(message = "Thời gian khám phải ở tương lai")
    private LocalDateTime appointmentTime;

    private String symptoms;
}
