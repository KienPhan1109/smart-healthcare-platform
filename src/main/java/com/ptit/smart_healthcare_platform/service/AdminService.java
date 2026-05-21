package com.ptit.smart_healthcare_platform.service;

import com.ptit.smart_healthcare_platform.model.dto.admin.MedicineRequestDto;
import com.ptit.smart_healthcare_platform.model.dto.admin.StaffRequestDto;
import com.ptit.smart_healthcare_platform.model.entity.*;
import com.ptit.smart_healthcare_platform.model.enums.DoctorStatus;
import com.ptit.smart_healthcare_platform.model.enums.MedicineStatus;
import com.ptit.smart_healthcare_platform.model.enums.RoleName;
import com.ptit.smart_healthcare_platform.model.enums.UserStatus;
import com.ptit.smart_healthcare_platform.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final DoctorRepository doctorRepository;
    private final SpecialtyRepository specialtyRepository;
    private final MedicineRepository medicineRepository;
    private final PaymentRepository paymentRepository;
    private final AppointmentRepository appointmentRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void createStaff(StaffRequestDto dto, String creator) {
        if (userRepository.existsByPhoneNumber(dto.getPhoneNumber())) {
            throw new RuntimeException("Số điện thoại đã tồn tại trong hệ thống");
        }

        User user = new User();
        user.setFullName(dto.getFullName());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setStatus(UserStatus.ACTIVE);
        user.setProfileCompleted(true);
        user.setCreatedBy(creator);

        Role role = roleRepository.findByName(dto.getRoleName())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chức vụ"));

        UserRole userRole = new UserRole();
        userRole.setUser(user);
        userRole.setRole(role);
        user.getUserRoles().add(userRole);

        user = userRepository.save(user);

        if (dto.getRoleName() == RoleName.ROLE_DOCTOR) {
            Specialty specialty = specialtyRepository.findById(dto.getSpecialtyId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy chuyên khoa"));

            Doctor doctor = new Doctor();
            doctor.setUser(user);
            doctor.setSpecialty(specialty);
            doctor.setExamFee(dto.getExamFee());
            doctor.setExperienceYears(dto.getExperienceYears());
            doctor.setAcademicRank(dto.getAcademicRank());
            doctor.setStatus(DoctorStatus.ACTIVE);
            doctor.setCreatedBy(creator);
            doctorRepository.save(doctor);
        }
    }

    public List<User> getAllStaffs() {
        return userRepository.findAll().stream()
                .filter(u -> u.getUserRoles().stream()
                        .anyMatch(ur -> ur.getRole().getName() != RoleName.ROLE_PATIENT && ur.getRole().getName() != RoleName.ROLE_ADMIN))
                .toList();
    }

    public List<Specialty> getAllSpecialties() {
        return specialtyRepository.findAll();
    }

    // --- Medicines ---
    public List<Medicine> getAllMedicines() {
        return medicineRepository.findAll();
    }

    public Medicine getMedicineById(Long id) {
        return medicineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thuốc"));
    }

    @Transactional
    public void saveMedicine(MedicineRequestDto dto, String creator) {
        Medicine medicine;
        if (dto.getId() != null) {
            medicine = getMedicineById(dto.getId());
            medicine.setUpdatedBy(creator);
            medicine.setUpdatedAt(LocalDateTime.now());
        } else {
            medicine = new Medicine();
            medicine.setCreatedBy(creator);
            medicine.setStatus(MedicineStatus.SELLING);
        }

        medicine.setName(dto.getName());
        medicine.setUnit(dto.getUnit());
        medicine.setPrice(dto.getPrice());
        medicine.setStockQuantity(dto.getStockQuantity());
        medicine.setUsageInstruction(dto.getUsageInstruction());

        medicineRepository.save(medicine);
    }

    @Transactional
    public void deleteMedicine(Long id) {
        Medicine medicine = getMedicineById(id);
        medicine.setStatus(MedicineStatus.STOPPED);
        medicineRepository.save(medicine);
    }

    // --- Dashboard ---
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        long totalPatients = userRepository.findAll().stream()
                .filter(u -> u.getUserRoles().stream().anyMatch(ur -> ur.getRole().getName() == RoleName.ROLE_PATIENT))
                .count();
        stats.put("totalPatients", totalPatients);

        long totalDoctors = doctorRepository.count();
        stats.put("totalDoctors", totalDoctors);

        long totalAppointments = appointmentRepository.count();
        stats.put("totalAppointments", totalAppointments);

        BigDecimal totalRevenue = paymentRepository.findAll().stream()
                .filter(p -> "PAID".equals(p.getStatus().name()))
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.put("totalRevenue", totalRevenue);

        // Revenue by month
        int currentYear = LocalDateTime.now().getYear();
        List<Object[]> monthlyData = paymentRepository.getMonthlyRevenueByYear(currentYear);
        BigDecimal[] monthlyRevenue = new BigDecimal[12];
        Arrays.fill(monthlyRevenue, BigDecimal.ZERO);
        for (Object[] row : monthlyData) {
            int month = ((Number) row[0]).intValue();
            BigDecimal rev = (BigDecimal) row[1];
            monthlyRevenue[month - 1] = rev;
        }
        stats.put("monthlyRevenue", monthlyRevenue);

        // Top 5 Doctors
        List<Object[]> topDoctorsRaw = appointmentRepository.findTopDoctorsByCompletedAppointments(PageRequest.of(0, 5));
        List<Map<String, Object>> topDoctors = new ArrayList<>();
        for (Object[] row : topDoctorsRaw) {
            Doctor d = (Doctor) row[0];
            Long total = ((Number) row[1]).longValue();
            Map<String, Object> map = new HashMap<>();
            map.put("doctorName", d.getFullName());
            map.put("specialty", d.getSpecialty().getName());
            map.put("completedCount", total);
            topDoctors.add(map);
        }
        stats.put("topDoctors", topDoctors);

        return stats;
    }
}
