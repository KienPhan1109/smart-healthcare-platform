package com.ptit.smart_healthcare_platform.controller;

import com.ptit.smart_healthcare_platform.model.dto.auth.RegisterRequestDto;
import com.ptit.smart_healthcare_platform.model.entity.Patient;
import com.ptit.smart_healthcare_platform.model.entity.User;
import com.ptit.smart_healthcare_platform.model.enums.Gender;
import com.ptit.smart_healthcare_platform.model.enums.PatientRelation;
import com.ptit.smart_healthcare_platform.repository.PatientRepository;
import com.ptit.smart_healthcare_platform.repository.UserRepository;
import com.ptit.smart_healthcare_platform.service.AuthService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Controller
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
    private final org.springframework.security.authentication.AuthenticationManager authenticationManager;
    private final com.ptit.smart_healthcare_platform.config.CustomSuccessHandler successHandler;

    public AuthController(AuthService authService,
                          UserRepository userRepository,
                          PatientRepository patientRepository,
                          org.springframework.security.crypto.password.PasswordEncoder passwordEncoder,
                          org.springframework.security.authentication.AuthenticationManager authenticationManager,
                          com.ptit.smart_healthcare_platform.config.CustomSuccessHandler successHandler) {
        this.authService = authService;
        this.userRepository = userRepository;
        this.patientRepository = patientRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.successHandler = successHandler;
    }

    @GetMapping("/login")
    public String loginPage(Model model) {
        model.addAttribute("loginRequest", new com.ptit.smart_healthcare_platform.model.dto.auth.LoginRequestDto());
        return "auth/login";
    }

    @PostMapping("/login")
    public String processLogin(@Valid @ModelAttribute("loginRequest") com.ptit.smart_healthcare_platform.model.dto.auth.LoginRequestDto dto,
                               BindingResult bindingResult,
                               jakarta.servlet.http.HttpServletRequest request,
                               jakarta.servlet.http.HttpServletResponse response,
                               Model model) {
        if (bindingResult.hasErrors()) {
            return "auth/login";
        }
        try {
            org.springframework.security.authentication.UsernamePasswordAuthenticationToken token = 
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(dto.getPhoneNumber(), dto.getPassword());
            Authentication authentication = authenticationManager.authenticate(token);
            org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(authentication);

            HttpSession session = request.getSession(true);
            session.setAttribute(org.springframework.security.web.context.HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, 
                org.springframework.security.core.context.SecurityContextHolder.getContext());

            // Goi truc tiep successHandler de thuc hien redirect dung chuan bao mat va logic ho so
            try {
                successHandler.onAuthenticationSuccess(request, response, authentication);
            } catch (Exception e) {
                // Xử lý ngoại lệ redirect nếu có
            }
            return null; // Response da duoc committed boi successHandler.sendRedirect
        } catch (org.springframework.security.core.AuthenticationException e) {
            bindingResult.reject("loginError", "Số điện thoại hoặc mật khẩu không chính xác");
            return "auth/login";
        }
    }

    @GetMapping("/register")
    public String registerPage(Model model, HttpSession session) {
        // Reset session dang ky cu neu co
        session.removeAttribute("reg_phone");
        session.removeAttribute("reg_otp");
        session.removeAttribute("reg_otp_verified");

        model.addAttribute("step", 1);
        model.addAttribute("step1Dto", new com.ptit.smart_healthcare_platform.model.dto.auth.RegisterStep1Dto());
        model.addAttribute("registerRequest", new RegisterRequestDto()); // Luon cung cap de tranh loi Thymeleaf
        return "auth/register";
    }

    @PostMapping("/register/step1")
    public String registerStep1(@Valid @ModelAttribute("step1Dto") com.ptit.smart_healthcare_platform.model.dto.auth.RegisterStep1Dto dto,
                                BindingResult bindingResult,
                                Model model,
                                HttpSession session) {
        // Luon cung cap registerRequest de tranh loi Thymeleaf
        model.addAttribute("registerRequest", new RegisterRequestDto());

        if (bindingResult.hasErrors()) {
            model.addAttribute("step", 1);
            return "auth/register";
        }

        // Kiem tra sdt trung lap
        if (userRepository.existsByPhoneNumber(dto.getPhoneNumber())) {
            bindingResult.rejectValue("phoneNumber", "duplicate", "Số điện thoại này đã được đăng ký. Vui lòng đăng nhập hoặc dùng số khác.");
            model.addAttribute("step", 1);
            return "auth/register";
        }

        // Sinh OTP gia lap
        String otp = String.format("%06d", (int) (Math.random() * 900000) + 100000);
        session.setAttribute("reg_phone", dto.getPhoneNumber());
        session.setAttribute("reg_otp", otp);
        session.setAttribute("reg_otp_verified", false);

        model.addAttribute("step", 2);
        model.addAttribute("regOtpCode", otp);
        return "auth/register";
    }

    @PostMapping("/register/step2")
    public String registerStep2(@RequestParam(required = false) String otpInput,
                                Model model,
                                HttpSession session) {
        // Luon cung cap registerRequest va step1Dto de tranh loi Thymeleaf
        model.addAttribute("step1Dto", new com.ptit.smart_healthcare_platform.model.dto.auth.RegisterStep1Dto());
        model.addAttribute("registerRequest", new RegisterRequestDto());

        String sessionPhone = (String) session.getAttribute("reg_phone");
        String sessionOtp = (String) session.getAttribute("reg_otp");

        if (sessionPhone == null || sessionOtp == null) {
            return "redirect:/register";
        }

        if (otpInput == null || otpInput.trim().length() != 6) {
            model.addAttribute("step", 2);
            model.addAttribute("regOtpCode", sessionOtp);
            model.addAttribute("otpError", "Vui lòng nhập đủ 6 chữ số của mã OTP");
            return "auth/register";
        }

        if (!otpInput.trim().matches("^[0-9]+$")) {
            model.addAttribute("step", 2);
            model.addAttribute("regOtpCode", sessionOtp);
            model.addAttribute("otpError", "Mã OTP chỉ được phép nhập chữ số");
            return "auth/register";
        }

        if (!sessionOtp.equals(otpInput.trim())) {
            model.addAttribute("step", 2);
            model.addAttribute("regOtpCode", sessionOtp);
            model.addAttribute("otpError", "Mã OTP không chính xác. Vui lòng kiểm tra lại.");
            return "auth/register";
        }

        session.setAttribute("reg_otp_verified", true);

        // Chuyen buoc 3
        RegisterRequestDto regRequest = new RegisterRequestDto();
        regRequest.setPhoneNumber(sessionPhone);

        model.addAttribute("step", 3);
        model.addAttribute("registerRequest", regRequest);
        return "auth/register";
    }

    @PostMapping("/auth/register")
    public String processRegister(@Valid @ModelAttribute("registerRequest") RegisterRequestDto dto,
                                  BindingResult bindingResult,
                                  RedirectAttributes redirectAttributes,
                                  HttpSession session,
                                  Model model) {
        String sessionPhone = (String) session.getAttribute("reg_phone");
        Boolean otpVerified = (Boolean) session.getAttribute("reg_otp_verified");

        if (sessionPhone == null || otpVerified == null || !otpVerified) {
            return "redirect:/register";
        }

        // Kiem tra password confirm
        if (dto.getPassword() != null && !dto.getPassword().equals(dto.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "mismatch", "Xác nhận mật khẩu không khớp với mật khẩu đã nhập");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("step", 3);
            model.addAttribute("step1Dto", new com.ptit.smart_healthcare_platform.model.dto.auth.RegisterStep1Dto());
            return "auth/register";
        }

        try {
            dto.setPhoneNumber(sessionPhone); // Dam bao lay dung sdt da verify OTP
            authService.registerPatient(dto);
            // Xoa session OTP dang ky
            session.removeAttribute("reg_phone");
            session.removeAttribute("reg_otp");
            session.removeAttribute("reg_otp_verified");
            redirectAttributes.addFlashAttribute("successMessage", "Đăng ký tài khoản thành công! Vui lòng đăng nhập.");
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            model.addAttribute("step", 3);
            model.addAttribute("step1Dto", new com.ptit.smart_healthcare_platform.model.dto.auth.RegisterStep1Dto());
            model.addAttribute("errorMessage", e.getMessage());
            return "auth/register";
        }
    }

    // Trang cap nhat thong tin co ban sau dang nhap lan dau
    @GetMapping("/auth/first-login-update")
    public String firstLoginUpdatePage(Authentication authentication, Model model) {
        String phoneNumber = authentication.getName();
        Optional<User> userOpt = userRepository.findByPhoneNumber(phoneNumber);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // Neu da hoan thanh thi chuyen thang vao dashboard
            if (user.getProfileCompleted()) {
                return "redirect:/patient/dashboard";
            }
            model.addAttribute("user", user);
        }

        if (!model.containsAttribute("updateRequest")) {
            model.addAttribute("updateRequest", new com.ptit.smart_healthcare_platform.model.dto.auth.FirstLoginUpdateRequestDto());
        }
        return "auth/first-login-update";
    }

    // Xu ly cap nhat thong tin hoac bo qua
    @PostMapping("/auth/first-login-update")
    public String processFirstLoginUpdate(Authentication authentication,
                                          @Valid @ModelAttribute("updateRequest") com.ptit.smart_healthcare_platform.model.dto.auth.FirstLoginUpdateRequestDto dto,
                                          BindingResult bindingResult,
                                          @RequestParam(required = false) String action,
                                          RedirectAttributes redirectAttributes,
                                          Model model) {

        String phoneNumber = authentication.getName();
        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new RuntimeException("Khong tim thay tai khoan"));

        if ("skip".equals(action)) {
            // Nguoi dung nhan "Cap nhat sau" -> danh dau da hoan thanh de khong hoi lai
            user.setProfileCompleted(true);
            user.setUpdatedBy(phoneNumber);
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
            return "redirect:/patient/dashboard";
        }

        // Validate ngay sinh neu co nhap
        if (dto.getDateOfBirth() != null && !dto.getDateOfBirth().isBlank()) {
            try {
                LocalDate dob = LocalDate.parse(dto.getDateOfBirth());
                if (dob.isAfter(LocalDate.now())) {
                    bindingResult.rejectValue("dateOfBirth", "invalid.dob", "Ngày sinh không được lớn hơn ngày hiện tại");
                }
            } catch (Exception e) {
                bindingResult.rejectValue("dateOfBirth", "invalid.dob", "Ngày sinh không hợp lệ");
            }
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("user", user);
            return "auth/first-login-update";
        }

        // Cap nhat thong tin User
        String email = dto.getEmail();
        if (email != null && !email.isBlank()) {
            if (userRepository.existsByEmail(email) && !email.equals(user.getEmail())) {
                bindingResult.rejectValue("email", "duplicate", "Email đã được sử dụng bởi tài khoản khác");
                model.addAttribute("user", user);
                return "auth/first-login-update";
            }
            user.setEmail(email);
        }

        user.setProfileCompleted(true);
        user.setUpdatedBy(phoneNumber);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        // Cap nhat ho so benh nhan mac danh (SELF)
        Optional<Patient> selfPatientOpt = patientRepository.findByUserIdAndRelation(user.getId(), PatientRelation.SELF);
        if (selfPatientOpt.isPresent()) {
            Patient self = selfPatientOpt.get();
            if (dto.getDateOfBirth() != null && !dto.getDateOfBirth().isBlank()) {
                self.setDateOfBirth(LocalDate.parse(dto.getDateOfBirth()));
            }
            if (dto.getGender() != null && !dto.getGender().isBlank()) {
                self.setGender(Gender.valueOf(dto.getGender()));
            }
            if (dto.getIdentityCard() != null && !dto.getIdentityCard().isBlank()) {
                self.setIdentityCard(dto.getIdentityCard());
            }

            // Cap nhat PatientProfile (BHYT)
            String insuranceNumber = dto.getInsuranceNumber();
            if (self.getPatientProfile() == null) {
                var profile = new com.ptit.smart_healthcare_platform.model.entity.PatientProfile();
                profile.setPatient(self);
                profile.setCreatedBy(phoneNumber);
                profile.setCreatedAt(LocalDateTime.now());
                if (insuranceNumber != null && !insuranceNumber.isBlank()) {
                    profile.setInsuranceNumber(insuranceNumber);
                }
                self.setPatientProfile(profile);
            } else {
                if (insuranceNumber != null && !insuranceNumber.isBlank()) {
                    self.getPatientProfile().setInsuranceNumber(insuranceNumber);
                }
                self.getPatientProfile().setUpdatedBy(phoneNumber);
                self.getPatientProfile().setUpdatedAt(LocalDateTime.now());
            }

            self.setUpdatedBy(phoneNumber);
            self.setUpdatedAt(LocalDateTime.now());
            patientRepository.save(self);
        }

        return "redirect:/patient/dashboard";
    }

    // Luong Quen mat khau Server-side khong dung JS
    @GetMapping("/forgot-password")
    public String forgotPasswordPage(Model model, HttpSession session) {
        session.removeAttribute("forgot_phone");
        session.removeAttribute("forgot_otp");
        session.removeAttribute("forgot_otp_verified");

        model.addAttribute("step", 1);
        model.addAttribute("step1Dto", new com.ptit.smart_healthcare_platform.model.dto.auth.ForgotPasswordStep1Dto());
        model.addAttribute("resetDto", new com.ptit.smart_healthcare_platform.model.dto.auth.ForgotPasswordResetDto()); // Tranh loi Thymeleaf
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password/step1")
    public String forgotStep1(@Valid @ModelAttribute("step1Dto") com.ptit.smart_healthcare_platform.model.dto.auth.ForgotPasswordStep1Dto dto,
                              BindingResult bindingResult,
                              Model model,
                              HttpSession session) {
        // Tranh loi Thymeleaf
        model.addAttribute("resetDto", new com.ptit.smart_healthcare_platform.model.dto.auth.ForgotPasswordResetDto());

        if (bindingResult.hasErrors()) {
            model.addAttribute("step", 1);
            return "auth/forgot-password";
        }

        // Kiem tra sdt co ton tai trong DB khong
        if (!userRepository.existsByPhoneNumber(dto.getPhoneNumber())) {
            bindingResult.rejectValue("phoneNumber", "notfound", "Số điện thoại này chưa được đăng ký trong hệ thống");
            model.addAttribute("step", 1);
            return "auth/forgot-password";
        }

        // Sinh OTP gia lap
        String otp = String.format("%06d", (int) (Math.random() * 900000) + 100000);
        session.setAttribute("forgot_phone", dto.getPhoneNumber());
        session.setAttribute("forgot_otp", otp);
        session.setAttribute("forgot_otp_verified", false);

        model.addAttribute("step", 2);
        model.addAttribute("forgotOtpCode", otp);
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password/step2")
    public String forgotStep2(@RequestParam(required = false) String otpInput,
                              Model model,
                              HttpSession session) {
        // Tranh loi Thymeleaf
        model.addAttribute("step1Dto", new com.ptit.smart_healthcare_platform.model.dto.auth.ForgotPasswordStep1Dto());
        model.addAttribute("resetDto", new com.ptit.smart_healthcare_platform.model.dto.auth.ForgotPasswordResetDto());

        String sessionPhone = (String) session.getAttribute("forgot_phone");
        String sessionOtp = (String) session.getAttribute("forgot_otp");

        if (sessionPhone == null || sessionOtp == null) {
            return "redirect:/forgot-password";
        }

        if (otpInput == null || otpInput.trim().length() != 6) {
            model.addAttribute("step", 2);
            model.addAttribute("forgotOtpCode", sessionOtp);
            model.addAttribute("otpError", "Vui lòng nhập đủ 6 chữ số của mã OTP");
            return "auth/forgot-password";
        }

        if (!otpInput.trim().matches("^[0-9]+$")) {
            model.addAttribute("step", 2);
            model.addAttribute("forgotOtpCode", sessionOtp);
            model.addAttribute("otpError", "Mã OTP chỉ được phép nhập chữ số");
            return "auth/forgot-password";
        }

        if (!sessionOtp.equals(otpInput.trim())) {
            model.addAttribute("step", 2);
            model.addAttribute("forgotOtpCode", sessionOtp);
            model.addAttribute("otpError", "Mã OTP không chính xác. Vui lòng kiểm tra lại.");
            return "auth/forgot-password";
        }

        session.setAttribute("forgot_otp_verified", true);

        model.addAttribute("step", 3);
        model.addAttribute("resetDto", new com.ptit.smart_healthcare_platform.model.dto.auth.ForgotPasswordResetDto());
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password/step3")
    public String forgotStep3(@Valid @ModelAttribute("resetDto") com.ptit.smart_healthcare_platform.model.dto.auth.ForgotPasswordResetDto dto,
                              BindingResult bindingResult,
                              Model model,
                              HttpSession session) {
        // Tranh loi Thymeleaf
        model.addAttribute("step1Dto", new com.ptit.smart_healthcare_platform.model.dto.auth.ForgotPasswordStep1Dto());

        String sessionPhone = (String) session.getAttribute("forgot_phone");
        Boolean otpVerified = (Boolean) session.getAttribute("forgot_otp_verified");

        if (sessionPhone == null || otpVerified == null || !otpVerified) {
            return "redirect:/forgot-password";
        }

        if (dto.getNewPassword() != null && !dto.getNewPassword().equals(dto.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "mismatch", "Xác nhận mật khẩu không khớp với mật khẩu đã nhập");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("step", 3);
            return "auth/forgot-password";
        }

        // Dat lai mat khau
        User user = userRepository.findByPhoneNumber(sessionPhone)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản"));
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        user.setUpdatedBy(sessionPhone);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        // Xoa session OTP quen mat khau
        session.removeAttribute("forgot_phone");
        session.removeAttribute("forgot_otp");
        session.removeAttribute("forgot_otp_verified");

        model.addAttribute("step", 4);
        return "auth/forgot-password";
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "error/access-denied";
    }
}
