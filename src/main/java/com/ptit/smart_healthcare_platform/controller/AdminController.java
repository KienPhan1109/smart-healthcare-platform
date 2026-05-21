package com.ptit.smart_healthcare_platform.controller;

import com.ptit.smart_healthcare_platform.model.dto.admin.MedicineRequestDto;
import com.ptit.smart_healthcare_platform.model.dto.admin.StaffRequestDto;
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
}
