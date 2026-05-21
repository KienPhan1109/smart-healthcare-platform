package com.ptit.smart_healthcare_platform.service;

import com.ptit.smart_healthcare_platform.model.dto.admin.MedicineRequestDto;
import com.ptit.smart_healthcare_platform.model.dto.admin.StaffRequestDto;
import com.ptit.smart_healthcare_platform.model.entity.*;
import com.ptit.smart_healthcare_platform.model.enums.*;
import com.ptit.smart_healthcare_platform.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
        // Kiểm tra trùng email toàn hệ thống
        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            if (userRepository.existsByEmail(dto.getEmail())) {
                throw new RuntimeException("Email này đã được sử dụng bởi tài khoản khác");
            }
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

    // Lấy danh sách nhân sự (loại bỏ PATIENT, ADMIN, COORDINATOR)
    public List<User> getAllStaffs() {
        return userRepository.findAll().stream()
                .filter(u -> u.getUserRoles().stream()
                        .anyMatch(ur -> {
                            RoleName rn = ur.getRole().getName();
                            return rn == RoleName.ROLE_DOCTOR || rn == RoleName.ROLE_TECHNICIAN;
                        }))
                .toList();
    }

    public User getStaffById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân sự"));
    }

    // --- Cập nhật nhân sự (Phương án B: chỉ cho phép nếu không có lịch hẹn PENDING/CONFIRMED) ---
    @Transactional
    public void updateStaff(Long userId, StaffRequestDto dto, String updater) {
        User user = getStaffById(userId);
        checkStaffHasActiveAppointments(user);

        // Kiểm tra trùng email (trừ email hiện tại)
        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            if (!dto.getEmail().equals(user.getEmail()) && userRepository.existsByEmail(dto.getEmail())) {
                throw new RuntimeException("Email này đã được sử dụng bởi tài khoản khác");
            }
        }
        // Kiểm tra trùng SĐT (trừ SĐT hiện tại)
        if (!dto.getPhoneNumber().equals(user.getPhoneNumber()) && userRepository.existsByPhoneNumber(dto.getPhoneNumber())) {
            throw new RuntimeException("Số điện thoại đã tồn tại trong hệ thống");
        }

        user.setFullName(dto.getFullName());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setEmail(dto.getEmail());
        user.setUpdatedBy(updater);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        // Cập nhật thông tin Bác sĩ nếu là ROLE_DOCTOR
        boolean isDoctor = user.getUserRoles().stream()
                .anyMatch(ur -> ur.getRole().getName() == RoleName.ROLE_DOCTOR);
        if (isDoctor) {
            Doctor doctor = doctorRepository.findByUserId(userId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin bác sĩ"));
            if (dto.getSpecialtyId() != null) {
                Specialty specialty = specialtyRepository.findById(dto.getSpecialtyId())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy chuyên khoa"));
                doctor.setSpecialty(specialty);
            }
            if (dto.getExamFee() != null) {
                doctor.setExamFee(dto.getExamFee());
            }
            if (dto.getAcademicRank() != null) {
                doctor.setAcademicRank(dto.getAcademicRank());
            }
            if (dto.getExperienceYears() != null) {
                doctor.setExperienceYears(dto.getExperienceYears());
            }
            doctor.setUpdatedBy(updater);
            doctor.setUpdatedAt(LocalDateTime.now());
            doctorRepository.save(doctor);
        }
    }

    // --- Khóa nhân sự ---
    @Transactional
    public void lockStaff(Long userId, String updater) {
        User user = getStaffById(userId);
        checkStaffHasActiveAppointments(user);
        user.setStatus(UserStatus.LOCKED);
        user.setUpdatedBy(updater);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    // --- Mở khóa nhân sự ---
    @Transactional
    public void unlockStaff(Long userId, String updater) {
        User user = getStaffById(userId);
        user.setStatus(UserStatus.ACTIVE);
        user.setUpdatedBy(updater);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    // Kiểm tra nhân sự có lịch hẹn đang hoạt động không
    private void checkStaffHasActiveAppointments(User user) {
        boolean isDoctor = user.getUserRoles().stream()
                .anyMatch(ur -> ur.getRole().getName() == RoleName.ROLE_DOCTOR);
        if (isDoctor) {
            Doctor doctor = doctorRepository.findByUserId(user.getId()).orElse(null);
            if (doctor != null) {
                boolean hasActive = appointmentRepository.existsByDoctorIdAndStatusInAndIsDeletedFalse(
                        doctor.getId(),
                        List.of(AppointmentStatus.PENDING, AppointmentStatus.CONFIRMED,
                                AppointmentStatus.EXAMINING, AppointmentStatus.WAITING_FOR_DRUG_PAYMENT,
                                AppointmentStatus.WAITING_FOR_LAB, AppointmentStatus.READY_FOR_REEXAM)
                );
                if (hasActive) {
                    throw new RuntimeException("Không thể thao tác do bác sĩ này đang có lịch hẹn chưa hoàn thành");
                }
            }
        }
    }

    public List<Specialty> getAllSpecialties() {
        return specialtyRepository.findAll();
    }

    // --- Tổng quan Chuyên khoa cho Admin ---
    public List<Map<String, Object>> getSpecialtiesWithDoctorCount() {
        List<Specialty> specialties = specialtyRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Specialty s : specialties) {
            Map<String, Object> map = new HashMap<>();
            map.put("specialty", s);
            long count = doctorRepository.countBySpecialtyId(s.getId());
            map.put("doctorCount", count);
            result.add(map);
        }
        return result;
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

    // --- Khôi phục thuốc đã ngừng bán ---
    @Transactional
    public void restoreMedicine(Long id) {
        Medicine medicine = getMedicineById(id);
        medicine.setStatus(MedicineStatus.SELLING);
        medicine.setUpdatedAt(LocalDateTime.now());
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
        
        List<Appointment> recentAppointments = appointmentRepository.findTop5ByOrderByCreatedAtDesc();
        // Initialize lazy loads
        for(Appointment appt : recentAppointments) {
            if(appt.getPatient() != null) appt.getPatient().getFullName();
            if(appt.getDoctor() != null && appt.getDoctor().getUser() != null) appt.getDoctor().getUser().getFullName();
        }
        stats.put("recentAppointments", recentAppointments);

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
