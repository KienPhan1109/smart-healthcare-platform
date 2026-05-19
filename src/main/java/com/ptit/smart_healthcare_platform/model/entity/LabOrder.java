package com.ptit.smart_healthcare_platform.model.entity;

import com.ptit.smart_healthcare_platform.model.enums.LabOrderStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "lab_orders")
@Getter
@Setter
public class LabOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Phiếu chỉ định phụ thuộc vào một Lịch khám cụ thể
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id", nullable = false)
    private Appointment appointment;

    // Bác sĩ ra y lệnh
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    // Bệnh nhân thực hiện
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    // Trạng thái phiếu chỉ định
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LabOrderStatus status = LabOrderStatus.PENDING;

    // Lý do chỉ định xét nghiệm của bác sĩ
    @Column(name = "diagnosis_notes", columnDefinition = "TEXT")
    private String diagnosisNotes;

    // Xóa mềm
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "created_by", length = 50)
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_by", length = 50)
    private String updatedBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Danh sách các kết quả/chi tiết xét nghiệm
    @OneToMany(mappedBy = "labOrder", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<LabResult> labResults = new ArrayList<>();
}
