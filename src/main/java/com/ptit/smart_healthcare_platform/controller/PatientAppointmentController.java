package com.ptit.smart_healthcare_platform.controller;

import com.ptit.smart_healthcare_platform.model.enums.PaymentStatus;

import com.ptit.smart_healthcare_platform.model.dto.appointment.AppointmentBookingRequestDto;
import com.ptit.smart_healthcare_platform.model.entity.Appointment;
import com.ptit.smart_healthcare_platform.model.entity.Doctor;
import com.ptit.smart_healthcare_platform.model.entity.Payment;
import com.ptit.smart_healthcare_platform.model.entity.User;
import com.ptit.smart_healthcare_platform.model.enums.DoctorStatus;
import com.ptit.smart_healthcare_platform.model.enums.TimeSlot;
import com.ptit.smart_healthcare_platform.repository.DoctorRepository;
import com.ptit.smart_healthcare_platform.repository.SpecialtyRepository;
import com.ptit.smart_healthcare_platform.repository.UserRepository;
import com.ptit.smart_healthcare_platform.service.AppointmentService;
import com.ptit.smart_healthcare_platform.service.PatientProfileService;
import com.ptit.smart_healthcare_platform.service.PaymentSimulationService;
import jakarta.validation.Valid;
import com.ptit.smart_healthcare_platform.repository.MedicalRecordRepository;
import com.ptit.smart_healthcare_platform.model.entity.MedicalRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

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
    private final MedicalRecordRepository medicalRecordRepository;

    private static final int MAX_BOOKING_DAYS_AHEAD = 7;
    private static final DateTimeFormatter DATE_DISPLAY_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy (EEEE)");

    private User getLoggedInUser(Authentication authentication) {
        String phoneNumber = authentication.getName();
        return userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy tài khoản"));
    }

    /**
     * Luồng đặt lịch 4 bước thuần SSR:
     * GET /book                                    → Bước 1: Chọn Chuyên khoa
     * GET /book?specialtyId=1                      → Bước 2: Chọn Bác sĩ
     * GET /book?specialtyId=1&doctorId=5           → Bước 3: Chọn Ngày khám
     * GET /book?specialtyId=1&doctorId=5&date=...  → Bước 4: Chọn Slot giờ & Xác nhận
     */
    @GetMapping("/book")
    public String showBookingForm(
            @RequestParam(required = false) Long specialtyId,
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) String date,
            Model model, Authentication authentication) {
        User user = getLoggedInUser(authentication);

        // Luôn cần: danh sách chuyên khoa và hồ sơ bệnh nhân
        model.addAttribute("specialties", specialtyRepository.findAllByOrderByNameAsc());
        model.addAttribute("profiles", patientProfileService.getProfilesByUser(user.getId()));

        if (!model.containsAttribute("dto")) {
            AppointmentBookingRequestDto dto = new AppointmentBookingRequestDto();
            // Giữ lại giá trị đã chọn để pre-fill form
            if (doctorId != null) {
                dto.setDoctorId(doctorId);
            }
            if (date != null) {
                dto.setAppointmentDate(date);
            }
            model.addAttribute("dto", dto);
        }

        // Bước 1: Chỉ hiển thị dropdown chuyên khoa
        if (specialtyId == null) {
            return "patient/appointments/book";
        }

        // Bước 2: Hiển thị danh sách bác sĩ thuộc chuyên khoa
        model.addAttribute("selectedSpecialtyId", specialtyId);
        model.addAttribute("doctors", doctorRepository.findAllBySpecialtyIdAndStatus(specialtyId, DoctorStatus.ACTIVE));

        if (doctorId == null) {
            return "patient/appointments/book";
        }

        // Bước 3: Hiển thị ô chọn ngày khám + thông tin bác sĩ đã chọn
        Doctor selectedDoctor = doctorRepository.findById(doctorId).orElse(null);
        if (selectedDoctor == null || selectedDoctor.getStatus() != DoctorStatus.ACTIVE) {
            model.addAttribute("errorMessage", "Bác sĩ không tồn tại hoặc hiện không nhận bệnh nhân.");
            return "patient/appointments/book";
        }
        model.addAttribute("selectedDoctor", selectedDoctor);
        model.addAttribute("selectedDoctorId", doctorId);

        // Tính danh sách ngày khả dụng (hôm nay + 7 ngày tới)
        List<String[]> availableDates = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = 0; i <= MAX_BOOKING_DAYS_AHEAD; i++) {
            LocalDate d = today.plusDays(i);
            availableDates.add(new String[]{d.toString(), d.format(DATE_DISPLAY_FORMAT)});
        }
        model.addAttribute("availableDates", availableDates);

        if (date == null) {
            return "patient/appointments/book";
        }

        // Bước 4: Hiển thị lưới slot giờ khả dụng
        LocalDate selectedDate;
        try {
            selectedDate = LocalDate.parse(date);
        } catch (DateTimeParseException e) {
            model.addAttribute("errorMessage", "Định dạng ngày không hợp lệ.");
            return "patient/appointments/book";
        }

        // Validate ngày nằm trong khoảng cho phép
        if (selectedDate.isBefore(today) || selectedDate.isAfter(today.plusDays(MAX_BOOKING_DAYS_AHEAD))) {
            model.addAttribute("errorMessage", "Ngày khám phải nằm trong khoảng từ hôm nay đến " + MAX_BOOKING_DAYS_AHEAD + " ngày tới.");
            return "patient/appointments/book";
        }

        model.addAttribute("selectedDate", date);
        model.addAttribute("selectedDateDisplay", selectedDate.format(DATE_DISPLAY_FORMAT));

        // Tính danh sách slot khả dụng
        List<TimeSlot> availableSlots = appointmentService.getAvailableSlots(doctorId, selectedDate);
        model.addAttribute("availableSlots", availableSlots);
        model.addAttribute("allSlots", TimeSlot.values());

        return "patient/appointments/book";
    }

    @PostMapping("/book")
    public String processBooking(@Valid @ModelAttribute("dto") AppointmentBookingRequestDto dto,
                                 BindingResult bindingResult,
                                 @RequestParam(required = false) Long specialtyId,
                                 Model model, Authentication authentication, RedirectAttributes redirectAttributes) {
        String redirectUrl = "/patient/appointments/book"
                + (specialtyId != null ? "?specialtyId=" + specialtyId : "")
                + (dto.getDoctorId() != null ? "&doctorId=" + dto.getDoctorId() : "")
                + (dto.getAppointmentDate() != null ? "&date=" + dto.getAppointmentDate() : "");

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.dto", bindingResult);
            redirectAttributes.addFlashAttribute("dto", dto);
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng điền đầy đủ thông tin đặt lịch.");
            return "redirect:" + redirectUrl;
        }

        try {
            User user = getLoggedInUser(authentication);
            
            Appointment appointment = appointmentService.bookAppointment(dto, user.getId(), user.getPhoneNumber());
            return "redirect:/patient/appointments/pay/" + appointment.getId();
            
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("dto", dto);
            return "redirect:" + redirectUrl;
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
            
            if (LocalDateTime.now().isAfter(expiryTime) && payment.getStatus() == PaymentStatus.PENDING) {
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
            
            redirectAttributes.addFlashAttribute("successMessage", "Thanh toán phí khám thành công! Lịch hẹn của bạn đã sẵn sàng chờ bác sĩ gọi khám.");
            return "redirect:/patient/appointments/history";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/patient/appointments/pay/" + id;
        }
    }

    @GetMapping("/history")
    public String showHistory(@RequestParam(required = false, defaultValue = "ALL") String status, Model model, Authentication authentication) {
        User user = getLoggedInUser(authentication);
        model.addAttribute("appointments", appointmentService.getBookingHistory(user.getId(), status));
        model.addAttribute("currentStatus", status.toUpperCase());
        return "patient/appointments/history";
    }



    @GetMapping("/pay-drug/{id}")
    public String showDrugPaymentPage(@PathVariable Long id, Model model, Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            User user = getLoggedInUser(authentication);
            Payment payment = paymentSimulationService.getDrugFeePayment(id, user.getId());
            Appointment appointment = payment.getAppointment();

            model.addAttribute("payment", payment);
            model.addAttribute("appointment", appointment);
            
            MedicalRecord record = medicalRecordRepository.findByAppointmentId(id).orElse(null);
            model.addAttribute("medicalRecord", record);
            
            return "patient/appointments/pay-drug";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/patient/appointments/detail/" + id;
        }
    }

    @PostMapping("/pay-drug/{id}/simulate")
    public String simulateDrugPayment(@PathVariable Long id, @RequestParam String method, Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            User user = getLoggedInUser(authentication);
            paymentSimulationService.processSimulatedDrugPayment(id, method, user.getId(), user.getPhoneNumber());
            
            redirectAttributes.addFlashAttribute("successMessage", "Thanh toán đơn thuốc thành công! Quy trình khám đã hoàn tất.");
            return "redirect:/patient/appointments/detail/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/patient/appointments/pay-drug/" + id;
        }
    }

    @GetMapping("/cancel/{id}")
    public String showCancelPage(@PathVariable Long id, Model model, Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            User user = getLoggedInUser(authentication);
            Appointment appointment = appointmentService.getAppointmentByIdAndUser(id, user.getId());
            
            if (!appointment.isCancelable()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Lịch hẹn này không thể hủy được nữa.");
                return "redirect:/patient/appointments/history";
            }
            
            model.addAttribute("appointment", appointment);
            return "patient/appointments/cancel";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/patient/appointments/history";
        }
    }

    @PostMapping("/cancel/{id}")
    public String cancelAppointment(@PathVariable Long id, 
                                    @RequestParam(required = false) String cancelReason,
                                    Authentication authentication, 
                                    RedirectAttributes redirectAttributes) {
        try {
            User user = getLoggedInUser(authentication);
            String reason = (cancelReason != null && !cancelReason.trim().isEmpty()) 
                            ? cancelReason.trim() 
                            : "Bệnh nhân tự hủy trên hệ thống";
            
            appointmentService.cancelAppointment(id, reason, user.getId(), user.getPhoneNumber());
            
            redirectAttributes.addFlashAttribute("successMessage", "Đã hủy lịch hẹn thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", getFriendlyErrorMessage(e));
        }
        return "redirect:/patient/appointments/history";
    }

    @GetMapping("/detail/{id}")
    public String showAppointmentDetail(@PathVariable Long id, Model model, Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            User user = getLoggedInUser(authentication);
            Appointment appointment = appointmentService.getAppointmentByIdAndUser(id, user.getId());
            model.addAttribute("appointment", appointment);
            
            MedicalRecord record = appointmentService.getMedicalRecordWithPrescriptionByAppointmentId(id);
            model.addAttribute("medicalRecord", record);
            
            Payment drugPayment = null;
            try {
                drugPayment = paymentSimulationService.getDrugFeePayment(id, user.getId());
            } catch (Exception e) {
                // Bỏ qua nếu chưa có hóa đơn thuốc
            }
            model.addAttribute("drugPayment", drugPayment);

            return "patient/appointments/detail";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", getFriendlyErrorMessage(e));
            return "redirect:/patient/appointments/history";
        }
    }

    private String getFriendlyErrorMessage(Exception e) {
        String msg = e.getMessage();
        if (msg == null) return "Đã xảy ra lỗi không xác định.";

        if (msg.contains("Duplicate entry") || msg.contains("ConstraintViolationException") || msg.contains("constraint")) {
            return "Dữ liệu bị trùng lặp hoặc vi phạm ràng buộc hệ thống. Vui lòng thử lại hoặc liên hệ hỗ trợ.";
        }
        return msg;
    }
}
