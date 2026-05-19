package com.ptit.smart_healthcare_platform.model.enums;

public enum LabOrderStatus {
    PENDING,            // Chờ thanh toán phí xét nghiệm
    PAID,               // Đã thanh toán, chờ thực hiện
    COMPLETED,          // Kỹ thuật viên đã trả kết quả
    CANCELLED           // Hủy chỉ định
}
