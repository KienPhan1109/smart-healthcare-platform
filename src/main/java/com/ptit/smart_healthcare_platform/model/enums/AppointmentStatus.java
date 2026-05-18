package com.ptit.smart_healthcare_platform.model.enums;

public enum AppointmentStatus {
    PENDING,            // Đang chờ (Giữ chỗ thanh toán 3 phút)
    WAITING_FOR_LAB,    // Đang chờ kết quả cận lâm sàng
    LAB_COMPLETED,      // Đã hoàn tất xét nghiệm
    READY_FOR_REEXAM,   // Đã thông báo và đưa vào danh sách ưu tiên tái khám
    COMPLETED,          // Đã hoàn tất quy trình khám bệnh ngoại trú
    CANCELLED           // Đã hủy lịch khám
}
