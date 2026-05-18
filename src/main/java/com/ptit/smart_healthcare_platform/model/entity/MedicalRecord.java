package com.ptit.smart_healthcare_platform.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "medical_records")
@Getter
@Setter
public class MedicalRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Liên kết 1-1 với lịch hẹn (nguồn gốc ca khám)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id", unique = true)
    private Appointment appointment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    // Dữ liệu chẩn đoán y tế nhạy cảm - chỉ trả về qua DTO có phân quyền
    // Triệu chứng lâm sàng bác sĩ ghi nhận khi khám
    @Column(nullable = false, columnDefinition = "TEXT")
    private String symptoms;

    // Kết quả chẩn đoán chính
    @Column(nullable = false, columnDefinition = "TEXT")
    private String diagnosis;

    // Ghi chú thêm của bác sĩ
    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // CORE-06: Một bệnh án tạo ra tối đa một đơn thuốc
    @OneToOne(mappedBy = "medicalRecord", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Prescription prescription;
}
