package com.ptit.smart_healthcare_platform.controller;

import com.ptit.smart_healthcare_platform.model.dto.admin.MedicineRequestDto;
import com.ptit.smart_healthcare_platform.model.dto.admin.StaffRequestDto;
import com.ptit.smart_healthcare_platform.model.entity.Doctor;
import com.ptit.smart_healthcare_platform.model.entity.User;
import com.ptit.smart_healthcare_platform.model.enums.RoleName;
import com.ptit.smart_healthcare_platform.repository.DoctorRepository;
import com.ptit.smart_healthcare_platform.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final DoctorRepository doctorRepository;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("stats", adminService.getDashboardStats());
        return "admin/dashboard";
    }

    // --- Staff Management ---
    @GetMapping("/staffs")
    public String listStaffs(Model model) {
        model.addAttribute("staffs", adminService.getAllStaffs());
        return "admin/staffs/list";
    }

    @GetMapping("/staffs/create")
    public String showCreateStaffForm(Model model) {
        if (!model.containsAttribute("dto")) {
            model.addAttribute("dto", new StaffRequestDto());
        }
        model.addAttribute("specialties", adminService.getAllSpecialties());
        return "admin/staffs/form";
    }

    @PostMapping("/staffs/create")
    public String createStaff(@Valid @ModelAttribute("dto") StaffRequestDto dto,
                              BindingResult bindingResult,
                              Authentication authentication,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("specialties", adminService.getAllSpecialties());
            return "admin/staffs/form";
        }
        try {
            adminService.createStaff(dto, authentication.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Đã tạo tài khoản nhân viên thành công!");
            return "redirect:/admin/staffs";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("specialties", adminService.getAllSpecialties());
            return "admin/staffs/form";
        }
    }

    @GetMapping("/staffs/{id}/edit")
    public String showEditStaffForm(@PathVariable Long id, Model model) {
        User user = adminService.getStaffById(id);
        if (!model.containsAttribute("dto")) {
            StaffRequestDto dto = new StaffRequestDto();
            dto.setId(user.getId());
            dto.setFullName(user.getFullName());
            dto.setPhoneNumber(user.getPhoneNumber());
            dto.setEmail(user.getEmail());

            // Nếu là bác sĩ, load thêm thông tin doctor
            boolean isDoctor = user.getUserRoles().stream()
                    .anyMatch(ur -> ur.getRole().getName() == RoleName.ROLE_DOCTOR);
            if (isDoctor) {
                dto.setRoleName(RoleName.ROLE_DOCTOR);
                Doctor doctor = doctorRepository.findByUserId(id).orElse(null);
                if (doctor != null) {
                    dto.setSpecialtyId(doctor.getSpecialty().getId());
                    dto.setExamFee(doctor.getExamFee());
                    dto.setAcademicRank(doctor.getAcademicRank());
                    dto.setExperienceYears(doctor.getExperienceYears());
                }
            } else {
                dto.setRoleName(RoleName.ROLE_TECHNICIAN);
            }
            model.addAttribute("dto", dto);
        }
        model.addAttribute("specialties", adminService.getAllSpecialties());
        model.addAttribute("editMode", true);
        model.addAttribute("staffUser", user);
        return "admin/staffs/edit";
    }

    @PostMapping("/staffs/{id}/update")
    public String updateStaff(@PathVariable Long id,
                              @ModelAttribute("dto") StaffRequestDto dto,
                              Authentication authentication,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        try {
            adminService.updateStaff(id, dto, authentication.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật thông tin nhân viên thành công!");
            return "redirect:/admin/staffs";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("specialties", adminService.getAllSpecialties());
            model.addAttribute("editMode", true);
            model.addAttribute("staffUser", adminService.getStaffById(id));
            return "admin/staffs/edit";
        }
    }

    @PostMapping("/staffs/{id}/lock")
    public String lockStaff(@PathVariable Long id,
                            Authentication authentication,
                            RedirectAttributes redirectAttributes) {
        try {
            adminService.lockStaff(id, authentication.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Đã khóa tài khoản nhân viên!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/staffs";
    }

    @PostMapping("/staffs/{id}/unlock")
    public String unlockStaff(@PathVariable Long id,
                              Authentication authentication,
                              RedirectAttributes redirectAttributes) {
        try {
            adminService.unlockStaff(id, authentication.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Đã mở khóa tài khoản nhân viên!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/staffs";
    }

    // --- Medicine Management ---
    @GetMapping("/medicines")
    public String listMedicines(Model model) {
        model.addAttribute("medicines", adminService.getAllMedicines());
        return "admin/medicines/list";
    }

    @GetMapping("/medicines/create")
    public String showCreateMedicineForm(Model model) {
        if (!model.containsAttribute("dto")) {
            model.addAttribute("dto", new MedicineRequestDto());
        }
        return "admin/medicines/form";
    }

    @GetMapping("/medicines/{id}/edit")
    public String showEditMedicineForm(@PathVariable Long id, Model model) {
        if (!model.containsAttribute("dto")) {
            var med = adminService.getMedicineById(id);
            var dto = new MedicineRequestDto();
            dto.setId(med.getId());
            dto.setName(med.getName());
            dto.setUnit(med.getUnit());
            dto.setPrice(med.getPrice());
            dto.setStockQuantity(med.getStockQuantity());
            dto.setUsageInstruction(med.getUsageInstruction());
            model.addAttribute("dto", dto);
        }
        return "admin/medicines/form";
    }

    @PostMapping("/medicines/save")
    public String saveMedicine(@Valid @ModelAttribute("dto") MedicineRequestDto dto,
                               BindingResult bindingResult,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "admin/medicines/form";
        }
        try {
            adminService.saveMedicine(dto, authentication.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Lưu thuốc thành công!");
            return "redirect:/admin/medicines";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return dto.getId() != null ? "redirect:/admin/medicines/" + dto.getId() + "/edit" : "redirect:/admin/medicines/create";
        }
    }

    @PostMapping("/medicines/{id}/delete")
    public String deleteMedicine(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            adminService.deleteMedicine(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã dừng bán thuốc!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/medicines";
    }

    @PostMapping("/medicines/{id}/restore")
    public String restoreMedicine(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            adminService.restoreMedicine(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã mở bán lại thuốc thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/medicines";
    }

    // --- Specialty Overview ---
    @GetMapping("/specialties")
    public String listSpecialties(Model model) {
        model.addAttribute("specialtiesData", adminService.getSpecialtiesWithDoctorCount());
        return "admin/specialties/list";
    }
}
