package com.ptit.smart_healthcare_platform.model.enums;

public enum PaymentStatus {
    PENDING,            // Đang chờ thanh toán
    PAID,               // Đã thanh toán thành công
    CANCELLED,          // Bị hủy bỏ
    REFUNDED            // Đã hoàn tiền
}
