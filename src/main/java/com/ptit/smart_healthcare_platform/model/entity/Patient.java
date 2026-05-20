package com.ptit.smart_healthcare_platform.model.entity;

import com.ptit.smart_healthcare_platform.model.enums.Gender;
import com.ptit.smart_healthcare_platform.model.enums.PatientRelation;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "patients")
@Getter
@Setter
public class Patient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Mot tai khoan co nhieu ho so benh nhan (1-N)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Ho va ten benh nhan (co the khac voi chu tai khoan neu la ho so con/me...)
    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    // Moi quan he voi chu tai khoan
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PatientRelation relation = PatientRelation.SELF;

    // Ngay sinh - nullable khi moi tao, bat buoc khi dat kham
    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    // Gioi tinh - nullable khi moi tao, bat buoc khi dat kham
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Gender gender;

    // So CCCD - nullable, bat buoc khi dat kham
    @Column(name = "identity_card", unique = true, length = 20)
    private String identityCard;

    @Column(name = "created_by", length = 50)
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_by", length = 50)
    private String updatedBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @OneToOne(mappedBy = "patient", cascade = CascadeType.ALL)
    private PatientProfile patientProfile;
}
