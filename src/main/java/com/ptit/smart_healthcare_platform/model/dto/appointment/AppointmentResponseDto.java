package com.ptit.smart_healthcare_platform.model.dto.appointment;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class AppointmentResponseDto {
    private Long id;
    private Long patientId;
    private String patientName;
    private Long doctorId;
    private String doctorName;
    private String specialtyName;
    private Integer queueNumber;
    private LocalDateTime appointmentTime;
    private String status;
    private String symptoms;
    private String cancelReason;
    private LocalDateTime createdAt;
}
