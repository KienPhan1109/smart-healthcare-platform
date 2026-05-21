package com.ptit.smart_healthcare_platform.repository;

import com.ptit.smart_healthcare_platform.model.entity.Doctor;
import com.ptit.smart_healthcare_platform.model.enums.DoctorStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    List<Doctor> findAllBySpecialtyIdAndStatus(Long specialtyId, DoctorStatus status);
    List<Doctor> findAllBySpecialtyIdAndStatusAndUser_Status(Long specialtyId, DoctorStatus status, com.ptit.smart_healthcare_platform.model.enums.UserStatus userStatus);
    Optional<Doctor> findByUserId(Long userId);
    long countBySpecialtyId(Long specialtyId);
}
