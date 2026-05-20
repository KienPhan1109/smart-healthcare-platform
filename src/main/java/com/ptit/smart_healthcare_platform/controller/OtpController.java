package com.ptit.smart_healthcare_platform.controller;

import com.ptit.smart_healthcare_platform.repository.UserRepository;
import com.ptit.smart_healthcare_platform.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/api/auth")
public class OtpController {

    private final UserRepository userRepository;
    private final AuthService authService;

    public OtpController(UserRepository userRepository, AuthService authService) {
        this.userRepository = userRepository;
        this.authService = authService;
    }

    // Kiem tra so dien thoai da ton tai trong he thong chua
    @PostMapping("/check-phone")
    public ResponseEntity<Map<String, Object>> checkPhone(@RequestBody Map<String, String> body) {
        String phone = body.get("phoneNumber");
        boolean exists = userRepository.existsByPhoneNumber(phone);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    // Sinh ma OTP ngau nhien 6 chu so va luu vao Session
    @PostMapping("/send-otp")
    public ResponseEntity<Map<String, Object>> sendOtp(@RequestBody Map<String, String> body,
                                                       HttpSession session) {
        String phone = body.get("phoneNumber");
        String otp = String.format("%06d", new Random().nextInt(999999));

        // Luu OTP vao session de xac thuc
        session.setAttribute("otp_code", otp);
        session.setAttribute("otp_phone", phone);

        // Tra ve OTP truc tiep cho client hien thi (gia lap, chua tich hop SMS that)
        return ResponseEntity.ok(Map.of(
                "success", true,
                "otp", otp,
                "message", "Mã OTP đã được gửi tới " + phone
        ));
    }

    // Xac thuc ma OTP nguoi dung nhap
    @PostMapping("/verify-otp")
    public ResponseEntity<Map<String, Object>> verifyOtp(@RequestBody Map<String, String> body,
                                                         HttpSession session) {
        String inputOtp = body.get("otp");
        String sessionOtp = (String) session.getAttribute("otp_code");

        if (sessionOtp != null && sessionOtp.equals(inputOtp)) {
            session.setAttribute("otp_verified", true);
            return ResponseEntity.ok(Map.of("verified", true));
        }

        return ResponseEntity.ok(Map.of("verified", false, "message", "Mã OTP không chính xác"));
    }

    // Dat lai mat khau sau khi xac thuc OTP thanh cong
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(@RequestBody Map<String, String> body,
                                                              HttpSession session) {
        Boolean otpVerified = (Boolean) session.getAttribute("otp_verified");
        String otpPhone = (String) session.getAttribute("otp_phone");

        if (otpVerified == null || !otpVerified || otpPhone == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Chưa xác thực OTP"));
        }

        String newPassword = body.get("newPassword");
        String confirmPassword = body.get("confirmPassword");

        if (!newPassword.equals(confirmPassword)) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Mật khẩu xác nhận không khớp"));
        }

        if (newPassword.length() < 6) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Mật khẩu phải có ít nhất 6 ký tự"));
        }

        try {
            authService.resetPassword(otpPhone, newPassword);
            // Xoa session sau khi doi mat khau thanh cong
            session.removeAttribute("otp_code");
            session.removeAttribute("otp_phone");
            session.removeAttribute("otp_verified");
            return ResponseEntity.ok(Map.of("success", true, "message", "Đổi mật khẩu thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}
