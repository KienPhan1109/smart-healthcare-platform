package com.ptit.smart_healthcare_platform.controller;

import com.ptit.smart_healthcare_platform.model.dto.auth.ProfileUpdateRequestDto;
import com.ptit.smart_healthcare_platform.model.entity.User;
import com.ptit.smart_healthcare_platform.repository.UserRepository;
import com.ptit.smart_healthcare_platform.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserRepository userRepository;
    private final AuthService authService;

    private User getLoggedInUser(Authentication authentication) {
        if (authentication == null) {
            throw new IllegalStateException("Chưa đăng nhập");
        }
        return userRepository.findByPhoneNumberWithRoles(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy tài khoản"));
    }

    @GetMapping
    public String showProfileForm(Model model, Authentication authentication) {
        User user = getLoggedInUser(authentication);
        
        ProfileUpdateRequestDto dto = new ProfileUpdateRequestDto();
        dto.setFullName(user.getFullName());
        dto.setEmail(user.getEmail());
        
        model.addAttribute("user", user);
        model.addAttribute("dto", dto);
        model.addAttribute("pageTitle", "Chỉnh sửa thông tin tài khoản");
        
        return "profile/edit";
    }

    @PostMapping
    public String updateProfile(@Valid @ModelAttribute("dto") ProfileUpdateRequestDto dto,
                                BindingResult bindingResult,
                                Model model,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        User user = getLoggedInUser(authentication);
        
        // Kiểm tra thủ công tính hợp lệ của đổi mật khẩu để hiển thị lỗi chi tiết
        boolean hasOld = dto.getOldPassword() != null && !dto.getOldPassword().isBlank();
        boolean hasNew = dto.getNewPassword() != null && !dto.getNewPassword().isBlank();
        boolean hasConfirm = dto.getConfirmPassword() != null && !dto.getConfirmPassword().isBlank();
        
        if (hasOld || hasNew || hasConfirm) {
            if (!hasOld) {
                bindingResult.rejectValue("oldPassword", "error.oldPassword", "Vui lòng nhập mật khẩu hiện tại");
            }
            if (!hasNew) {
                bindingResult.rejectValue("newPassword", "error.newPassword", "Vui lòng nhập mật khẩu mới");
            }
            if (!hasConfirm) {
                bindingResult.rejectValue("confirmPassword", "error.confirmPassword", "Vui lòng xác nhận mật khẩu mới");
            }
            if (hasNew && dto.getNewPassword().length() < 6) {
                bindingResult.rejectValue("newPassword", "error.newPassword", "Mật khẩu mới phải có tối thiểu 6 ký tự");
            }
            if (hasNew && hasConfirm && !dto.getNewPassword().equals(dto.getConfirmPassword())) {
                bindingResult.rejectValue("confirmPassword", "error.confirmPassword", "Mật khẩu xác nhận không khớp với mật khẩu mới");
            }
            if (hasOld && hasNew && dto.getOldPassword().equals(dto.getNewPassword())) {
                bindingResult.rejectValue("newPassword", "error.newPassword", "Mật khẩu mới không được trùng với mật khẩu cũ");
            }
        }
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("user", user);
            model.addAttribute("pageTitle", "Chỉnh sửa thông tin tài khoản");
            return "profile/edit";
        }
        
        try {
            authService.updateProfile(user.getPhoneNumber(), dto);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thông tin tài khoản thành công!");
            return "redirect:/profile";
        } catch (Exception e) {
            bindingResult.reject("globalError", e.getMessage());
            model.addAttribute("user", user);
            model.addAttribute("pageTitle", "Chỉnh sửa thông tin tài khoản");
            return "profile/edit";
        }
    }
}
