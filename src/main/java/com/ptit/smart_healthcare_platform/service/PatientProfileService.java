package com.ptit.smart_healthcare_platform.service;

import com.ptit.smart_healthcare_platform.model.enums.BloodType;

import com.ptit.smart_healthcare_platform.model.dto.patient.PatientProfileCreateDto;
import com.ptit.smart_healthcare_platform.model.dto.patient.PatientProfileUpdateDto;
import com.ptit.smart_healthcare_platform.model.entity.Patient;
import com.ptit.smart_healthcare_platform.model.entity.PatientProfile;
import com.ptit.smart_healthcare_platform.model.entity.User;
import com.ptit.smart_healthcare_platform.repository.PatientProfileRepository;
import com.ptit.smart_healthcare_platform.repository.PatientRepository;
import com.ptit.smart_healthcare_platform.repository.UserRepository;
import com.ptit.smart_healthcare_platform.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PatientProfileService {

    private final PatientRepository patientRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;

    @Transactional(readOnly = true)
    public List<Patient> getProfilesByUser(Long userId) {
        return patientRepository.findAllByUserIdAndIsDeletedFalse(userId);
    }

    @Transactional(readOnly = true)
    public Patient getProfileByIdAndUser(Long id, Long userId) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hồ sơ bệnh nhân"));
        
        if (patient.getIsDeleted() != null && patient.getIsDeleted()) {
            throw new IllegalArgumentException("Hồ sơ bệnh nhân đã bị xóa");
        }
        
        if (!patient.getUser().getId().equals(userId)) {
            throw new SecurityException("Bạn không có quyền truy cập hồ sơ này");
        }
        
        return patient;
    }

    @Transactional
    public Patient createProfile(PatientProfileCreateDto dto, Long userId, String actor) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));

        // Chuyển đổi các chuỗi rỗng thành null để tránh vi phạm ràng buộc UNIQUE của DB
        String insNum = dto.getInsuranceNumber();
        if (insNum != null && insNum.trim().isEmpty()) {
            insNum = null;
        }
        String idCard = dto.getIdentityCard();
        if (idCard != null && idCard.trim().isEmpty()) {
            idCard = null;
        }

        // CORE-11: Validate BHYT format and uniqueness on creation
        if (insNum != null) {
            if (patientProfileRepository.findByInsuranceNumber(insNum).isPresent()) {
                throw new IllegalArgumentException("Số thẻ BHYT đã được sử dụng trong hệ thống");
            }
        }

        // Validate CCCD uniqueness on creation
        if (idCard != null) {
            if (patientRepository.findByIdentityCard(idCard).isPresent()) {
                throw new IllegalArgumentException("Số CCCD/CMND đã được sử dụng trong hệ thống");
            }
        }

        Patient patient = new Patient();
        patient.setUser(user);
        patient.setFullName(dto.getFullName());
        patient.setRelation(dto.getRelation());
        patient.setDateOfBirth(dto.getDateOfBirth());
        patient.setGender(dto.getGender());
        patient.setIdentityCard(idCard);
        patient.setCreatedBy(actor);
        patient.setCreatedAt(LocalDateTime.now());
        
        patient = patientRepository.save(patient);

        PatientProfile profile = new PatientProfile();
        profile.setPatient(patient);
        profile.setHeight(dto.getHeight() != null ? dto.getHeight() : java.math.BigDecimal.ZERO);
        profile.setWeight(dto.getWeight() != null ? dto.getWeight() : java.math.BigDecimal.ZERO);
        profile.setBloodType(dto.getBloodType() != null ? dto.getBloodType() : BloodType.UNKNOWN);
        profile.setMedicalHistory(dto.getMedicalHistory());
        profile.setAllergies(dto.getAllergies());
        profile.setInsuranceNumber(insNum);
        profile.setInsuranceExpiryDate(dto.getInsuranceExpiryDate());
        profile.setCreatedBy(actor);
        profile.setCreatedAt(LocalDateTime.now());
        patientProfileRepository.save(profile);

        return patient;
    }

    @Transactional
    public Patient updateProfile(Long id, PatientProfileUpdateDto dto, Long userId, String actor) {
        Patient patient = getProfileByIdAndUser(id, userId);

        // Chuyển đổi các chuỗi rỗng thành null để tránh vi phạm ràng buộc UNIQUE của DB
        String insNum = dto.getInsuranceNumber();
        if (insNum != null && insNum.trim().isEmpty()) {
            insNum = null;
        }
        String idCard = dto.getIdentityCard();
        if (idCard != null && idCard.trim().isEmpty()) {
            idCard = null;
        }

        // CORE-11: Validate BHYT uniqueness on update
        if (insNum != null) {
            patientProfileRepository.findByInsuranceNumber(insNum).ifPresent(existing -> {
                if (!existing.getId().equals(id)) {
                    throw new IllegalArgumentException("Số thẻ BHYT đã được sử dụng trong hệ thống");
                }
            });
        }

        // Validate CCCD uniqueness on update
        if (idCard != null) {
            patientRepository.findByIdentityCard(idCard).ifPresent(existing -> {
                if (!existing.getId().equals(id)) {
                    throw new IllegalArgumentException("Số CCCD/CMND đã được sử dụng trong hệ thống");
                }
            });
        }

        // Cap nhat thong tin thuc the Patient
        patient.setFullName(dto.getFullName());
        patient.setDateOfBirth(dto.getDateOfBirth());
        patient.setGender(dto.getGender());
        patient.setIdentityCard(idCard);
        patient.setUpdatedBy(actor);
        patient.setUpdatedAt(LocalDateTime.now());
        patientRepository.save(patient);

        PatientProfile profile = patientProfileRepository.findByPatientId(patient.getId())
                .orElseGet(() -> {
                    PatientProfile newProfile = new PatientProfile();
                    newProfile.setPatient(patient);
                    newProfile.setCreatedBy(actor);
                    newProfile.setCreatedAt(LocalDateTime.now());
                    return newProfile;
                });

        profile.setHeight(dto.getHeight() != null ? dto.getHeight() : java.math.BigDecimal.ZERO);
        profile.setWeight(dto.getWeight() != null ? dto.getWeight() : java.math.BigDecimal.ZERO);
        profile.setBloodType(dto.getBloodType() != null ? dto.getBloodType() : BloodType.UNKNOWN);
        profile.setMedicalHistory(dto.getMedicalHistory());
        profile.setAllergies(dto.getAllergies());
        profile.setInsuranceNumber(insNum);
        profile.setInsuranceExpiryDate(dto.getInsuranceExpiryDate());
        
        profile.setUpdatedBy(actor);
        profile.setUpdatedAt(LocalDateTime.now());

        patientProfileRepository.save(profile);

        return patient;
    }

    @Transactional
    public void deleteProfile(Long id, Long userId) {
        Patient patient = getProfileByIdAndUser(id, userId);
        
        if (patient.getRelation() == com.ptit.smart_healthcare_platform.model.enums.PatientRelation.SELF) {
            throw new IllegalArgumentException("Không được phép xóa hồ sơ của bản thân.");
        }
        
        if (appointmentRepository.existsByPatientIdAndIsDeletedFalse(id)) {
            throw new IllegalArgumentException("Không thể xóa hồ sơ bệnh nhân này vì đã có lịch hẹn khám trên hệ thống");
        }

        patient.setIsDeleted(true);
        patient.setDeletedAt(LocalDateTime.now());
        patientRepository.save(patient);
    }
}
