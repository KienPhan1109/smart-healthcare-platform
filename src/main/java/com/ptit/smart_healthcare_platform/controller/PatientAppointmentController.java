package com.ptit.smart_healthcare_platform.controller;

import com.ptit.smart_healthcare_platform.model.dto.appointment.AppointmentBookingRequestDto;
import com.ptit.smart_healthcare_platform.model.entity.Appointment;
import com.ptit.smart_healthcare_platform.model.entity.Payment;
import com.ptit.smart_healthcare_platform.model.entity.User;
import com.ptit.smart_healthcare_platform.model.enums.DoctorStatus;
import com.ptit.smart_healthcare_platform.repository.DoctorRepository;
import com.ptit.smart_healthcare_platform.repository.SpecialtyRepository;
import com.ptit.smart_healthcare_platform.repository.UserRepository;
import com.ptit.smart_healthcare_platform.service.AppointmentService;
import com.ptit.smart_healthcare_platform.service.PatientProfileService;
import com.ptit.smart_healthcare_platform.service.PaymentSimulationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.Duration;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/patient/appointments")
@RequiredArgsConstructor
public class PatientAppointmentController {

    private final AppointmentService appointmentService;
    private final PatientProfileService patientProfileService;
    private final SpecialtyRepository specialtyRepository;
    private final DoctorRepository doctorRepository;
    private final PaymentSimulationService paymentSimulationService;
    private final UserRepository userRepository;

    private User getLoggedInUser(Authentication authentication) {
        String phoneNumber = authentication.getName();
        return userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy tài khoản"));
    }

    @GetMapping("/book")
    public String showBookingForm(
            @RequestParam(required = false) Long specialtyId,
            Model model, Authentication authentication) {
        User user = getLoggedInUser(authentication);
        
        model.addAttribute("profiles", patientProfileService.getProfilesByUser(user.getId()));
        model.addAttribute("specialties", specialtyRepository.findAllByOrderByNameAsc());
        
        if (specialtyId != null) {
            model.addAttribute("selectedSpecialtyId", specialtyId);
            model.addAttribute("doctors", doctorRepository.findAllBySpecialtyIdAndStatus(specialtyId, DoctorStatus.ACTIVE));
        }

        if (!model.containsAttribute("dto")) {
            model.addAttribute("dto", new AppointmentBookingRequestDto());
        }
        
        return "patient/appointments/book";
    }

    @PostMapping("/book")
    public String processBooking(@Valid @ModelAttribute("dto") AppointmentBookingRequestDto dto,
                                 BindingResult bindingResult,
                                 @RequestParam(required = false) Long specialtyId,
                                 Model model, Authentication authentication, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.dto", bindingResult);
            redirectAttributes.addFlashAttribute("dto", dto);
            return "redirect:/patient/appointments/book" + (specialtyId != null ? "?specialtyId=" + specialtyId : "");
        }

        try {
            User user = getLoggedInUser(authentication);
            
            Appointment appointment = appointmentService.bookAppointment(dto, user.getId(), user.getPhoneNumber());
            return "redirect:/patient/appointments/pay/" + appointment.getId();
            
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("dto", dto);
            return "redirect:/patient/appointments/book" + (specialtyId != null ? "?specialtyId=" + specialtyId : "");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Đã xảy ra lỗi: " + e.getMessage());
            return "redirect:/patient/appointments/book";
        }
    }

    @GetMapping("/pay/{id}")
    public String showPaymentPage(@PathVariable Long id, Model model, Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            User user = getLoggedInUser(authentication);
            Payment payment = paymentSimulationService.getExamFeePayment(id, user.getId());
            Appointment appointment = payment.getAppointment();

            LocalDateTime expiryTime = appointment.getCreatedAt().plusMinutes(3);
            
            if (LocalDateTime.now().isAfter(expiryTime) && payment.getStatus() == com.ptit.smart_healthcare_platform.model.enums.PaymentStatus.PENDING) {
                model.addAttribute("isExpired", true);
                model.addAttribute("secondsRemaining", 0);
            } else {
                long secondsRemaining = Duration.between(LocalDateTime.now(), expiryTime).getSeconds();
                model.addAttribute("isExpired", false);
                model.addAttribute("secondsRemaining", Math.max(0, secondsRemaining));
            }

            model.addAttribute("payment", payment);
            model.addAttribute("appointment", appointment);
            
            return "patient/appointments/pay";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/patient/appointments/history";
        }
    }

    @PostMapping("/pay/{id}/simulate")
    public String simulatePayment(@PathVariable Long id, @RequestParam String method, Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            User user = getLoggedInUser(authentication);
            
            paymentSimulationService.processSimulatedPayment(id, method, user.getId(), user.getPhoneNumber());
            
            redirectAttributes.addFlashAttribute("successMessage", "Thanh toán phí khám thành công! Lịch hẹn của bạn đã được ghi nhận và đang chờ điều phối viên duyệt.");
            return "redirect:/patient/appointments/history";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/patient/appointments/pay/" + id;
        }
    }

    @GetMapping("/history")
    public String showHistory(Model model, Authentication authentication) {
        User user = getLoggedInUser(authentication);
        model.addAttribute("appointments", appointmentService.getBookingHistory(user.getId()));
        return "patient/appointments/history";
    }

    @PostMapping("/cancel/{id}")
    public String cancelAppointment(@PathVariable Long id, Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            User user = getLoggedInUser(authentication);
            
            appointmentService.cancelAppointment(id, "Bệnh nhân tự hủy trên hệ thống", user.getId(), user.getPhoneNumber());
            
            redirectAttributes.addFlashAttribute("successMessage", "Đã hủy lịch hẹn thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/patient/appointments/history";
    }
}
