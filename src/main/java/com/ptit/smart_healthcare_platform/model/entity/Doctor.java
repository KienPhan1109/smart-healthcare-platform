package com.ptit.smart_healthcare_platform.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "doctors")
@Getter
@Setter
public class Doctor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "specialty_id", nullable = false)
    private Specialty specialty;

    // Họ và tên bác sĩ hiển thị cho bệnh nhân
    @Column(nullable = false, length = 100)
    private String fullName;

    // Học hàm / Học vị chuyên môn hiển thị cho bệnh nhân đặt lịch (VD: PGS.TS, BS.CKII, ThS.BS)
    @Column(name = "academic_rank", length = 50)
    private String academicRank;

    @Column(nullable = false, unique = true, length = 15)
    private String phoneNumber;

    @Column(columnDefinition = "TEXT")
    private String biography;

    @Column(nullable = false)
    private Integer experienceYears = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private com.ptit.smart_healthcare_platform.model.enums.DoctorStatus status = com.ptit.smart_healthcare_platform.model.enums.DoctorStatus.ACTIVE;

    @OneToMany(mappedBy = "doctor", fetch = FetchType.LAZY)
    private List<Appointment> appointments = new ArrayList<>();

    @OneToMany(mappedBy = "doctor", fetch = FetchType.LAZY)
    private List<MedicalRecord> medicalRecords = new ArrayList<>();
}
