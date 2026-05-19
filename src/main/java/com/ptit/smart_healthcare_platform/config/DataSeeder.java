package com.ptit.smart_healthcare_platform.config;

import com.ptit.smart_healthcare_platform.model.entity.*;
import com.ptit.smart_healthcare_platform.model.enums.*;
import com.ptit.smart_healthcare_platform.repository.PatientRepository;
import com.ptit.smart_healthcare_platform.repository.RoleRepository;
import com.ptit.smart_healthcare_platform.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(RoleRepository roleRepository,
                      UserRepository userRepository,
                      PatientRepository patientRepository,
                      PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.patientRepository = patientRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (roleRepository.count() > 0) {
            System.out.println("[DataSeeder] Du lieu da ton tai, bo qua seed.");
            return;
        }

        System.out.println("[DataSeeder] Bat dau nap du lieu mau...");

        // === 1. Tao 6 Role ===
        Role rolePatient = createRole(RoleName.ROLE_PATIENT);
        Role roleDoctor = createRole(RoleName.ROLE_DOCTOR);
        Role roleCoordinator = createRole(RoleName.ROLE_COORDINATOR);
        Role rolePharmacist = createRole(RoleName.ROLE_PHARMACIST);
        Role roleCashier = createRole(RoleName.ROLE_CASHIER);
        Role roleAdmin = createRole(RoleName.ROLE_ADMIN);

        // === 2. Tao tai khoan mau - Mat khau chung: 123456 ===
        User patient = createUser("0901000001", "Nguyen Van Benh", "patient1@shp.vn", true, rolePatient);
        createUser("0901000002", "Tran Thi Bac Si", "doctor1@shp.vn", true, roleDoctor);
        createUser("0901000003", "Le Van Dieu Phoi", "coordinator1@shp.vn", true, roleCoordinator);
        createUser("0901000004", "Pham Thi Duoc Si", "pharmacist1@shp.vn", true, rolePharmacist);
        createUser("0901000005", "Hoang Van Thu Ngan", "cashier1@shp.vn", true, roleCashier);
        createUser("0901000006", "Quan Tri Vien", "admin@shp.vn", true, roleAdmin);

        // === 3. Tao ho so benh nhan mac dinh (SELF) cho tai khoan benh nhan ===
        Patient selfPatient = new Patient();
        selfPatient.setUser(patient);
        selfPatient.setFullName(patient.getFullName());
        selfPatient.setRelation(PatientRelation.SELF);
        selfPatient.setCreatedBy("SYSTEM");
        selfPatient.setCreatedAt(LocalDateTime.now());
        patientRepository.save(selfPatient);

        System.out.println("[DataSeeder] Hoan tat! 6 Role + 6 tai khoan mau da duoc tao.");
        System.out.println("[DataSeeder] Dang nhap bang so dien thoai, mat khau: 123456");
        System.out.println("  - 0901000001 (Benh nhan) / 0901000002 (Bac si) / 0901000003 (Dieu phoi)");
        System.out.println("  - 0901000004 (Duoc si) / 0901000005 (Thu ngan) / 0901000006 (Admin)");
    }

    private Role createRole(RoleName name) {
        Role role = new Role();
        role.setName(name);
        return roleRepository.save(role);
    }

    private User createUser(String phone, String fullName, String email,
                            boolean profileCompleted, Role role) {
        User user = new User();
        user.setPhoneNumber(phone);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode("123456"));
        user.setFullName(fullName);
        user.setStatus(UserStatus.ACTIVE);
        user.setProfileCompleted(profileCompleted);
        user.setCreatedBy("SYSTEM");
        user.setCreatedAt(LocalDateTime.now());

        user = userRepository.save(user);

        UserRole userRole = new UserRole();
        userRole.setUser(user);
        userRole.setRole(role);
        userRole.setCreatedBy("SYSTEM");
        userRole.setCreatedAt(LocalDateTime.now());

        user.getUserRoles().add(userRole);
        userRepository.save(user);

        return user;
    }
}
