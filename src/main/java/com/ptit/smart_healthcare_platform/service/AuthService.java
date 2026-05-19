package com.ptit.smart_healthcare_platform.service;

import com.ptit.smart_healthcare_platform.model.dto.auth.RegisterRequestDto;
import com.ptit.smart_healthcare_platform.model.entity.Patient;
import com.ptit.smart_healthcare_platform.model.entity.Role;
import com.ptit.smart_healthcare_platform.model.entity.User;
import com.ptit.smart_healthcare_platform.model.entity.UserRole;
import com.ptit.smart_healthcare_platform.model.enums.PatientRelation;
import com.ptit.smart_healthcare_platform.model.enums.RoleName;
import com.ptit.smart_healthcare_platform.model.enums.UserStatus;
import com.ptit.smart_healthcare_platform.repository.PatientRepository;
import com.ptit.smart_healthcare_platform.repository.RoleRepository;
import com.ptit.smart_healthcare_platform.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PatientRepository patientRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PatientRepository patientRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.patientRepository = patientRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User registerPatient(RegisterRequestDto dto) {
        // Kiem tra trung so dien thoai
        if (userRepository.existsByPhoneNumber(dto.getPhoneNumber())) {
            throw new IllegalArgumentException("So dien thoai da duoc su dung");
        }

        // Kiem tra mat khau khop
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new IllegalArgumentException("Mat khau xac nhan khong khop");
        }

        // Tao User moi - khong co username, email de null
        User user = new User();
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setFullName(dto.getFullName());
        user.setStatus(UserStatus.ACTIVE);
        user.setProfileCompleted(false);
        user.setCreatedBy("SYSTEM");
        user.setCreatedAt(LocalDateTime.now());

        user = userRepository.save(user);

        // Gan quyen ROLE_PATIENT mac dinh
        Role patientRole = roleRepository.findByName(RoleName.ROLE_PATIENT)
                .orElseThrow(() -> new RuntimeException("Role ROLE_PATIENT chua ton tai trong DB"));

        UserRole userRole = new UserRole();
        userRole.setUser(user);
        userRole.setRole(patientRole);
        userRole.setCreatedBy("SYSTEM");
        userRole.setCreatedAt(LocalDateTime.now());

        user.getUserRoles().add(userRole);
        user = userRepository.save(user);

        // Tu dong tao ho so benh nhan mac dinh (SELF) - thong tin chua day du
        Patient selfPatient = new Patient();
        selfPatient.setUser(user);
        selfPatient.setFullName(dto.getFullName());
        selfPatient.setRelation(PatientRelation.SELF);
        selfPatient.setCreatedBy("SYSTEM");
        selfPatient.setCreatedAt(LocalDateTime.now());

        patientRepository.save(selfPatient);

        return user;
    }

    @Transactional
    public void resetPassword(String phoneNumber, String newPassword) {
        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay tai khoan"));

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedBy(phoneNumber);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }
}
