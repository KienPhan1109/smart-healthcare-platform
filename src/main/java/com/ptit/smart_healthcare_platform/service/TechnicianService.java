package com.ptit.smart_healthcare_platform.service;

import com.ptit.smart_healthcare_platform.model.entity.Appointment;
import com.ptit.smart_healthcare_platform.model.entity.LabOrder;
import com.ptit.smart_healthcare_platform.model.entity.LabOrderDetail;
import com.ptit.smart_healthcare_platform.model.enums.AppointmentStatus;
import com.ptit.smart_healthcare_platform.repository.AppointmentRepository;
import com.ptit.smart_healthcare_platform.repository.LabOrderDetailRepository;
import com.ptit.smart_healthcare_platform.repository.LabOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TechnicianService {

    private final LabOrderRepository labOrderRepository;
    private final LabOrderDetailRepository labOrderDetailRepository;
    private final AppointmentRepository appointmentRepository;

    /**
     * Lấy danh sách chi tiết xét nghiệm đã thanh toán (PAID) theo phòng xét nghiệm.
     * Chỉ trả về các xét nghiệm chưa có kết quả (result == null).
     */
    @Transactional(readOnly = true)
    public List<LabOrderDetail> getPendingDetailsByRoom(String roomType) {
        List<LabOrder> paidOrders = labOrderRepository.findAllByStatusAndIsDeletedFalse("PAID");
        return paidOrders.stream()
                .flatMap(order -> order.getDetails().stream())
                .filter(detail -> detail.getLabTest().getRoomType().equals(roomType))
                .filter(detail -> detail.getResult() == null || detail.getResult().isBlank())
                .collect(Collectors.toList());
    }

    /**
     * Nhập kết quả xét nghiệm cho một chi tiết xét nghiệm cụ thể.
     * Nếu tất cả các xét nghiệm trong phiếu chỉ định đã hoàn tất,
     * tự động cập nhật trạng thái LabOrder → COMPLETED và Appointment → READY_FOR_REEXAM.
     */
    @Transactional
    public void submitResult(Long detailId, String result, String actor) {
        LabOrderDetail detail = labOrderDetailRepository.findById(detailId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chi tiết xét nghiệm"));

        if (detail.getResult() != null && !detail.getResult().isBlank()) {
            throw new IllegalStateException("Xét nghiệm này đã được nhập kết quả trước đó");
        }

        // Cập nhật kết quả
        detail.setResult(result);
        detail.setCompletedAt(LocalDateTime.now());
        detail.setUpdatedBy(actor);
        detail.setUpdatedAt(LocalDateTime.now());
        labOrderDetailRepository.save(detail);

        // Kiểm tra xem tất cả các chi tiết trong phiếu chỉ định đã hoàn tất chưa
        LabOrder labOrder = detail.getLabOrder();
        boolean allCompleted = labOrder.getDetails().stream()
                .allMatch(d -> d.getResult() != null && !d.getResult().isBlank());

        if (allCompleted) {
            // Cập nhật trạng thái phiếu chỉ định → COMPLETED
            labOrder.setStatus("COMPLETED");
            labOrder.setUpdatedBy(actor);
            labOrder.setUpdatedAt(LocalDateTime.now());
            labOrderRepository.save(labOrder);

            // Cập nhật trạng thái lịch hẹn → READY_FOR_REEXAM (Sẵn sàng tái khám ưu tiên)
            Appointment appointment = labOrder.getAppointment();
            appointment.setStatus(AppointmentStatus.READY_FOR_REEXAM);
            appointment.setUpdatedBy(actor);
            appointment.setUpdatedAt(LocalDateTime.now());
            appointmentRepository.save(appointment);
        }
    }

    /**
     * Lấy chi tiết xét nghiệm theo ID (để hiển thị form nhập kết quả).
     */
    @Transactional(readOnly = true)
    public LabOrderDetail getDetailById(Long detailId) {
        return labOrderDetailRepository.findById(detailId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chi tiết xét nghiệm"));
    }
}
