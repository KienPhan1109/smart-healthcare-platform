package com.ptit.smart_healthcare_platform.controller;

import com.ptit.smart_healthcare_platform.model.entity.Appointment;
import com.ptit.smart_healthcare_platform.model.entity.User;
import com.ptit.smart_healthcare_platform.model.enums.AppointmentStatus;
import com.ptit.smart_healthcare_platform.repository.AppointmentRepository;
import com.ptit.smart_healthcare_platform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;

    @GetMapping("/patient/dashboard")
    public String patientDashboard(Model model, Authentication authentication) {
        User user = userRepository.findByPhoneNumberWithRoles(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản"));

        // Lấy tất cả appointment của bệnh nhân
        List<Appointment> allAppointments = appointmentRepository
                .findAllByPatientUserIdAndIsDeletedFalseOrderByAppointmentTimeDesc(user.getId());

        // Đếm ca sắp tới (chưa hoàn thành, chưa hủy)
        long upcomingCount = allAppointments.stream()
                .filter(a -> a.getStatus() != AppointmentStatus.COMPLETED
                          && a.getStatus() != AppointmentStatus.CANCELLED)
                .count();

        // Đếm ca đã hoàn thành
        long completedCount = allAppointments.stream()
                .filter(a -> a.getStatus() == AppointmentStatus.COMPLETED)
                .count();

        // 5 lịch hẹn gần nhất
        List<Appointment> recentAppointments = allAppointments.stream()
                .limit(5)
                .toList();

        model.addAttribute("userName", user.getFullName());
        model.addAttribute("upcomingCount", upcomingCount);
        model.addAttribute("completedCount", completedCount);
        model.addAttribute("totalCount", allAppointments.size());
        model.addAttribute("recentAppointments", recentAppointments);

        return "patient/dashboard";
    }
}
