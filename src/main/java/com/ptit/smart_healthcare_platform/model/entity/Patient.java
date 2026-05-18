package com.ptit.smart_healthcare_platform.model.entity;

import com.ptit.smart_healthcare_platform.model.enums.Gender;
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

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    private LocalDate dateOfBirth;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Gender gender = Gender.OTHER;

    @Column(name = "created_by", length = 50)
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_by", length = 50)
    private String updatedBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToOne(mappedBy = "patient", cascade = CascadeType.ALL)
    private PatientProfile patientProfile;

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
