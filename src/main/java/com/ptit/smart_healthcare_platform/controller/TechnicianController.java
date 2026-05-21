package com.ptit.smart_healthcare_platform.controller;

import com.ptit.smart_healthcare_platform.model.entity.LabOrderDetail;
import com.ptit.smart_healthcare_platform.service.TechnicianService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.ptit.smart_healthcare_platform.model.dto.technician.LabResultRequestDto;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/technician")
@RequiredArgsConstructor
public class TechnicianController {

    private final TechnicianService technicianService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // Đếm số lượng xét nghiệm đang chờ theo từng phòng
        long hematologyCount = technicianService.getPendingDetailsByRoom("HEMATOLOGY").size();
        long ultrasoundCount = technicianService.getPendingDetailsByRoom("ULTRASOUND").size();
        long imagingCount = technicianService.getPendingDetailsByRoom("IMAGING").size();

        model.addAttribute("hematologyCount", hematologyCount);
        model.addAttribute("ultrasoundCount", ultrasoundCount);
        model.addAttribute("imagingCount", imagingCount);
        model.addAttribute("totalPending", hematologyCount + ultrasoundCount + imagingCount);

        return "technician/dashboard";
    }

    @GetMapping("/room/{roomType}")
    public String showRoom(@PathVariable String roomType, Model model) {
        // Validate roomType
        if (!roomType.equals("HEMATOLOGY") && !roomType.equals("ULTRASOUND") && !roomType.equals("IMAGING")) {
            return "redirect:/technician/dashboard";
        }

        List<LabOrderDetail> pendingDetails = technicianService.getPendingDetailsByRoom(roomType);
        model.addAttribute("pendingDetails", pendingDetails);
        model.addAttribute("roomType", roomType);

        // Tên phòng hiển thị tiếng Việt
        switch (roomType) {
            case "HEMATOLOGY": model.addAttribute("roomName", "Phòng Huyết học & Hóa sinh"); break;
            case "ULTRASOUND": model.addAttribute("roomName", "Phòng Siêu âm & Thăm dò chức năng"); break;
            case "IMAGING": model.addAttribute("roomName", "Phòng Chẩn đoán hình ảnh"); break;
        }

        return "technician/room";
    }

    @GetMapping("/fill-result/{detailId}")
    public String showFillResultForm(@PathVariable Long detailId, Model model, RedirectAttributes redirectAttributes) {
        try {
            LabOrderDetail detail = technicianService.getDetailById(detailId);
            model.addAttribute("detail", detail);
            if (!model.containsAttribute("dto")) {
                model.addAttribute("dto", new LabResultRequestDto());
            }
            return "technician/fill-result";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/technician/dashboard";
        }
    }

    @PostMapping("/fill-result/{detailId}")
    public String submitResult(@PathVariable Long detailId,
                               @Valid @ModelAttribute("dto") LabResultRequestDto dto,
                               BindingResult bindingResult,
                               Authentication authentication,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        try {
            if (bindingResult.hasErrors()) {
                LabOrderDetail detail = technicianService.getDetailById(detailId);
                model.addAttribute("detail", detail);
                return "technician/fill-result";
            }

            technicianService.submitResult(detailId, dto.getResult().trim(), authentication.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Đã trả kết quả xét nghiệm thành công!");

            // Quay lại phòng xét nghiệm tương ứng
            LabOrderDetail detail = technicianService.getDetailById(detailId);
            return "redirect:/technician/room/" + detail.getLabTest().getRoomType();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/technician/fill-result/" + detailId;
        }
    }
}
