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
import java.util.Optional;

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
            throw new IllegalArgumentException("Số điện thoại đã được sử dụng");
        }

        // Kiem tra mat khau khop
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new IllegalArgumentException("Mật khẩu xác nhận không khớp");
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
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản"));

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedBy(phoneNumber);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Transactional
    public void updateProfile(String currentPhone, com.ptit.smart_healthcare_platform.model.dto.auth.ProfileUpdateRequestDto dto) {
        User user = userRepository.findByPhoneNumber(currentPhone)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản"));

        // 1. Kiểm tra và cập nhật mật khẩu nếu có yêu cầu đổi mật khẩu
        boolean hasOldPassword = dto.getOldPassword() != null && !dto.getOldPassword().isBlank();
        boolean hasNewPassword = dto.getNewPassword() != null && !dto.getNewPassword().isBlank();
        boolean hasConfirmPassword = dto.getConfirmPassword() != null && !dto.getConfirmPassword().isBlank();

        if (hasOldPassword || hasNewPassword || hasConfirmPassword) {
            if (!hasOldPassword) {
                throw new IllegalArgumentException("Vui lòng nhập mật khẩu hiện tại");
            }
            if (!hasNewPassword) {
                throw new IllegalArgumentException("Vui lòng nhập mật khẩu mới");
            }
            if (!hasConfirmPassword) {
                throw new IllegalArgumentException("Vui lòng xác nhận mật khẩu mới");
            }
            if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
                throw new IllegalArgumentException("Mật khẩu hiện tại không chính xác");
            }
            if (dto.getNewPassword().length() < 6) {
                throw new IllegalArgumentException("Mật khẩu mới phải có tối thiểu 6 ký tự");
            }
            if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
                throw new IllegalArgumentException("Mật khẩu xác nhận không khớp với mật khẩu mới");
            }
            user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        }

        // 2. Kiểm tra và cập nhật email
        String email = dto.getEmail();
        if (email != null && !email.isBlank()) {
            if (userRepository.existsByEmail(email) && !email.equals(user.getEmail())) {
                throw new IllegalArgumentException("Địa chỉ Email đã được sử dụng bởi tài khoản khác");
            }
            user.setEmail(email);
        } else {
            user.setEmail(null);
        }

        // 3. Cập nhật Họ và tên
        user.setFullName(dto.getFullName());
        user.setUpdatedBy(currentPhone);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        // 4. Đồng bộ họ tên lên hồ sơ bệnh nhân gốc (SELF) của Patient nếu có
        Optional<Patient> selfPatientOpt = patientRepository.findByUserIdAndRelation(user.getId(), PatientRelation.SELF);
        if (selfPatientOpt.isPresent()) {
            Patient self = selfPatientOpt.get();
            self.setFullName(dto.getFullName());
            self.setUpdatedBy(currentPhone);
            self.setUpdatedAt(LocalDateTime.now());
            patientRepository.save(self);
        }
    }
}
