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
public class Doctor extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "specialty_id", nullable = false)
    private Specialty specialty;

    // Học hàm / Học vị chuyên môn hiển thị cho bệnh nhân đặt lịch (VD: PGS.TS, BS.CKII, ThS.BS)
    @Column(name = "academic_rank", length = 50)
    private String academicRank;

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

    // Các phương thức ủy quyền (Delegate) sang đối tượng User để bảo toàn tính tương thích ngược
    public String getFullName() {
        return user != null ? user.getFullName() : null;
    }

    public void setFullName(String fullName) {
        if (this.user != null) {
            this.user.setFullName(fullName);
        }
    }

    public String getPhoneNumber() {
        return user != null ? user.getPhoneNumber() : null;
    }

    public void setPhoneNumber(String phoneNumber) {
        if (this.user != null) {
            this.user.setPhoneNumber(phoneNumber);
        }
    }
}

