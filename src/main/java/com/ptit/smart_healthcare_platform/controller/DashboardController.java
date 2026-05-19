package com.ptit.smart_healthcare_platform.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @GetMapping("/patient/dashboard")
    public String patientDashboard() {
        return "patient/dashboard";
    }

    @GetMapping("/doctor/dashboard")
    public String doctorDashboard() {
        return "doctor/dashboard";
    }

    @GetMapping("/coordinator/dashboard")
    public String coordinatorDashboard() {
        return "coordinator/dashboard";
    }

    @GetMapping("/pharmacist/dashboard")
    public String pharmacistDashboard() {
        return "pharmacist/dashboard";
    }

    @GetMapping("/cashier/dashboard")
    public String cashierDashboard() {
        return "cashier/dashboard";
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard() {
        return "admin/dashboard";
    }
}
