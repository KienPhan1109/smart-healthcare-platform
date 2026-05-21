package com.ptit.smart_healthcare_platform.config;

import com.ptit.smart_healthcare_platform.model.entity.User;
import com.ptit.smart_healthcare_platform.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Component
public class CustomSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;

    public CustomSuccessHandler(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        String phoneNumber = authentication.getName(); // principal la phoneNumber

        // Kiem tra benh nhan dang nhap lan dau (profileCompleted = false)
        boolean isPatient = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_PATIENT"));

        if (isPatient) {
            Optional<User> userOpt = userRepository.findByPhoneNumber(phoneNumber);
            if (userOpt.isPresent() && !userOpt.get().getProfileCompleted()) {
                response.sendRedirect(request.getContextPath() + "/auth/first-login-update");
                return;
            }
        }

        // Phan luong dashboard theo role uu tien
        String redirectUrl = determineTargetUrl(authentication);
        response.sendRedirect(request.getContextPath() + redirectUrl);
    }

    private String determineTargetUrl(Authentication authentication) {
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            String role = authority.getAuthority();
            switch (role) {
                case "ROLE_ADMIN": return "/admin/dashboard";
                case "ROLE_DOCTOR": return "/doctor/dashboard";
                case "ROLE_TECHNICIAN": return "/technician/dashboard";
                case "ROLE_PATIENT": return "/patient/dashboard";
            }
        }
        return "/login?error";
    }
}
