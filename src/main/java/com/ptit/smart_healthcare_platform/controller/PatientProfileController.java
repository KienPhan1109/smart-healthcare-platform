package com.ptit.smart_healthcare_platform.controller;

import com.ptit.smart_healthcare_platform.model.dto.patient.PatientProfileCreateDto;
import com.ptit.smart_healthcare_platform.model.dto.patient.PatientProfileUpdateDto;
import com.ptit.smart_healthcare_platform.model.entity.Patient;
import com.ptit.smart_healthcare_platform.model.entity.PatientProfile;
import com.ptit.smart_healthcare_platform.model.entity.User;
import com.ptit.smart_healthcare_platform.repository.PatientProfileRepository;
import com.ptit.smart_healthcare_platform.repository.UserRepository;
import com.ptit.smart_healthcare_platform.service.PatientProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/patient/profiles")
@RequiredArgsConstructor
public class PatientProfileController {

    private final PatientProfileService patientProfileService;
    private final PatientProfileRepository patientProfileRepository;
    private final UserRepository userRepository;

    private User getLoggedInUser(Authentication authentication) {
        String phoneNumber = authentication.getName();
        return userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy tài khoản"));
    }

    @GetMapping
    public String listProfiles(Model model, Authentication authentication) {
        User user = getLoggedInUser(authentication);
        model.addAttribute("profiles", patientProfileService.getProfilesByUser(user.getId()));
        return "patient/profiles/list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("dto", new PatientProfileCreateDto());
        return "patient/profiles/create";
    }

    @PostMapping("/create")
    public String createProfile(@Valid @ModelAttribute("dto") PatientProfileCreateDto dto,
                                BindingResult bindingResult,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "patient/profiles/create";
        }

        try {
            User user = getLoggedInUser(authentication);
            patientProfileService.createProfile(dto, user.getId(), user.getPhoneNumber());
            redirectAttributes.addFlashAttribute("successMessage", "Thêm hồ sơ thành công!");
            return "redirect:/patient/profiles";
        } catch (Exception e) {
            bindingResult.reject("globalError", e.getMessage());
            return "patient/profiles/create";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            User user = getLoggedInUser(authentication);
            Patient patient = patientProfileService.getProfileByIdAndUser(id, user.getId());
            PatientProfile profile = patientProfileRepository.findByPatientId(id).orElse(new PatientProfile());

            PatientProfileUpdateDto dto = new PatientProfileUpdateDto();
            dto.setFullName(patient.getFullName());
            dto.setDateOfBirth(patient.getDateOfBirth());
            dto.setGender(patient.getGender());
            dto.setIdentityCard(patient.getIdentityCard());
            dto.setHeight(profile.getHeight());
            dto.setWeight(profile.getWeight());
            dto.setBloodType(profile.getBloodType());
            dto.setMedicalHistory(profile.getMedicalHistory());
            dto.setAllergies(profile.getAllergies());
            dto.setInsuranceNumber(profile.getInsuranceNumber());
            dto.setInsuranceExpiryDate(profile.getInsuranceExpiryDate());

            model.addAttribute("patient", patient);
            model.addAttribute("dto", dto);
            return "patient/profiles/edit";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/patient/profiles";
        }
    }

    @PostMapping("/edit/{id}")
    public String updateProfile(@PathVariable Long id,
                                @Valid @ModelAttribute("dto") PatientProfileUpdateDto dto,
                                BindingResult bindingResult,
                                Model model,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        try {
            User user = getLoggedInUser(authentication);
            
            if (bindingResult.hasErrors()) {
                Patient patient = patientProfileService.getProfileByIdAndUser(id, user.getId());
                model.addAttribute("patient", patient);
                return "patient/profiles/edit";
            }

            patientProfileService.updateProfile(id, dto, user.getId(), user.getPhoneNumber());
            
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật hồ sơ thành công!");
            return "redirect:/patient/profiles";
        } catch (Exception e) {
            try {
                User user = getLoggedInUser(authentication);
                Patient patient = patientProfileService.getProfileByIdAndUser(id, user.getId());
                model.addAttribute("patient", patient);
            } catch (Exception ignored) {}
            bindingResult.reject("globalError", e.getMessage());
            return "patient/profiles/edit";
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteProfile(@PathVariable Long id, Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            User user = getLoggedInUser(authentication);
            patientProfileService.deleteProfile(id, user.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Xóa hồ sơ bệnh nhân thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/patient/profiles";
    }
}
