package com.ptit.smart_healthcare_platform.model.entity;

import com.ptit.smart_healthcare_platform.model.enums.AppointmentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "appointments",
        uniqueConstraints = {
                // CORE-05: Chống xung đột - cùng 1 bác sĩ không bị đặt trùng khung giờ
                @UniqueConstraint(columnNames = {"doctor_id", "appointment_time"})
        })
@Getter
@Setter
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    // Người tạo lịch: Bệnh nhân tự đặt hoặc Điều phối viên đặt hộ
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    // Số thứ tự khám trong ngày (Điều phối viên quản lý)
    private Integer queueNumber;

    // Thời điểm khám - bắt buộc phải ở tương lai
    @Column(name = "appointment_time", nullable = false)
    private LocalDateTime appointmentTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AppointmentStatus status = AppointmentStatus.PENDING;

    // Mô tả triệu chứng ban đầu của bệnh nhân khi đặt lịch
    @Column(columnDefinition = "TEXT")
    private String symptoms;

    // CORE-09: Lý do hủy lịch (chỉ có giá trị khi status = CANCELLED)
    @Column(columnDefinition = "TEXT")
    private String cancelReason;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // LOẠI BỎ quan hệ 1-1 hai chiều ngược (mappedBy) với MedicalRecord để:
    // 1. Tránh EAGER loading bệnh án khi truy xuất lịch khám của bệnh nhân
    // 2. Khắc phục cảnh báo OneToOne từ IDE
}
