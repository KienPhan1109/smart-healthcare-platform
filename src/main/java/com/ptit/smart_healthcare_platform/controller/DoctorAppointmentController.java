package com.ptit.smart_healthcare_platform.controller;

import com.ptit.smart_healthcare_platform.model.dto.doctor.ExaminationFormDto;
import com.ptit.smart_healthcare_platform.model.entity.Appointment;
import com.ptit.smart_healthcare_platform.model.entity.Doctor;
import com.ptit.smart_healthcare_platform.model.entity.LabOrder;
import com.ptit.smart_healthcare_platform.model.entity.User;
import com.ptit.smart_healthcare_platform.model.enums.MedicineStatus;
import com.ptit.smart_healthcare_platform.repository.AppointmentRepository;
import com.ptit.smart_healthcare_platform.repository.LabTestRepository;
import com.ptit.smart_healthcare_platform.repository.MedicineRepository;
import com.ptit.smart_healthcare_platform.repository.UserRepository;
import com.ptit.smart_healthcare_platform.service.DoctorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ptit.smart_healthcare_platform.model.enums.AppointmentStatus;
import java.util.List;

@Controller
@RequestMapping("/doctor")
@RequiredArgsConstructor
public class DoctorAppointmentController {

    private final DoctorService doctorService;
    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;
    private final MedicineRepository medicineRepository;
    private final LabTestRepository labTestRepository;

    private Doctor getCurrentDoctor(Authentication authentication) {
        User user = userRepository.findByPhoneNumber(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user"));
        return doctorService.getDoctorProfile(user.getId());
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        Doctor doctor = getCurrentDoctor(authentication);
        List<Appointment> todayAppointments = doctorService.getTodayAppointments(doctor.getId());
        
        long waitingCount = todayAppointments.stream()
                .filter(a -> a.getStatus() == AppointmentStatus.CONFIRMED)
                .count();
        long completedCount = todayAppointments.stream()
                .filter(a -> a.getStatus() == AppointmentStatus.COMPLETED || a.getStatus() == AppointmentStatus.WAITING_FOR_DRUG_PAYMENT)
                .count();
        long reexamCount = todayAppointments.stream()
                .filter(a -> a.getStatus() == AppointmentStatus.READY_FOR_REEXAM)
                .count();

        model.addAttribute("doctor", doctor);
        model.addAttribute("appointments", todayAppointments);
        model.addAttribute("waitingCount", waitingCount);
        model.addAttribute("completedCount", completedCount);
        model.addAttribute("reexamCount", reexamCount);
        return "doctor/dashboard";
    }



    @GetMapping("/appointments/{id}/examine")
    public String showExaminationForm(@PathVariable Long id, Authentication authentication, Model model, RedirectAttributes redirectAttributes) {
        try {
            Doctor doctor = getCurrentDoctor(authentication);
            Appointment appt = appointmentRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lịch hẹn"));
            
            if (!appt.getDoctor().getId().equals(doctor.getId())) {
                throw new SecurityException("Không có quyền truy cập");
            }
            
            // Chuyển sang đang khám
            doctorService.startExamining(appt.getId(), doctor.getId());

            model.addAttribute("appointment", appt);
            model.addAttribute("medicines", medicineRepository.findAllByStatusAndStockQuantityGreaterThan(MedicineStatus.SELLING, 0));
            model.addAttribute("labTests", labTestRepository.findAllByOrderByNameAsc());
            
            // Nạp kết quả xét nghiệm nếu có (cho tái khám)
            LabOrder labOrder = doctorService.getLabOrderByAppointmentId(id);
            model.addAttribute("labOrder", labOrder);
            
            if (!model.containsAttribute("examinationForm")) {
                model.addAttribute("examinationForm", new ExaminationFormDto());
            }
            
            return "doctor/examine";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/doctor/dashboard";
        }
    }

    @PostMapping("/appointments/{id}/examine")
    public String submitExamination(@PathVariable Long id, 
                                    @Valid @ModelAttribute("examinationForm") ExaminationFormDto form,
                                    BindingResult bindingResult,
                                    Authentication authentication,
                                    RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.examinationForm", bindingResult);
            redirectAttributes.addFlashAttribute("examinationForm", form);
            return "redirect:/doctor/appointments/" + id + "/examine";
        }

        try {
            Doctor doctor = getCurrentDoctor(authentication);
            doctorService.submitExamination(id, doctor.getId(), form, authentication.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Hoàn tất khám và kê đơn thành công");
            return "redirect:/doctor/dashboard";
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("examinationForm", form);
            return "redirect:/doctor/appointments/" + id + "/examine";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Đã xảy ra lỗi hệ thống: " + (e.getMessage() != null ? e.getMessage() : "Dữ liệu không hợp lệ"));
            redirectAttributes.addFlashAttribute("examinationForm", form);
            return "redirect:/doctor/appointments/" + id + "/examine";
        }
    }

    @PostMapping("/appointments/{id}/lab-order")
    public String submitLabOrder(@PathVariable Long id,
                                 @RequestParam(required = false) List<Long> labTestIds,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {
        try {
            if (labTestIds == null || labTestIds.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng chọn ít nhất một xét nghiệm cận lâm sàng.");
                return "redirect:/doctor/appointments/" + id + "/examine";
            }
            Doctor doctor = getCurrentDoctor(authentication);
            doctorService.submitLabOrder(id, doctor.getId(), labTestIds, authentication.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Chỉ định xét nghiệm thành công. Bệnh nhân sẽ được chuyển sang thanh toán phí cận lâm sàng.");
            return "redirect:/doctor/dashboard";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/doctor/appointments/" + id + "/examine";
        }
    }
}
