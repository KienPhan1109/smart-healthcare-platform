package com.ptit.smart_healthcare_platform.repository;

import com.ptit.smart_healthcare_platform.model.entity.Patient;
import com.ptit.smart_healthcare_platform.model.enums.PatientRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    // Tim ho so benh nhan mac dinh (SELF) cua mot tai khoan
    Optional<Patient> findByUserIdAndRelation(Long userId, PatientRelation relation);

    // Lay tat ca ho so benh nhan cua mot tai khoan
    List<Patient> findAllByUserId(Long userId);
}
