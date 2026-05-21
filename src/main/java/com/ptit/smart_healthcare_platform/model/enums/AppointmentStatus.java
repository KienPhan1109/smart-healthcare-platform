package com.ptit.smart_healthcare_platform.model.enums;

public enum AppointmentStatus {
    PENDING,                    // Đang chờ thanh toán phí khám (giữ chỗ 3 phút)
    CONFIRMED,                  // Đã thanh toán, chờ đến giờ khám
    EXAMINING,                  // Đang khám (bệnh nhân đã xác nhận vào)
    WAITING_FOR_DRUG_PAYMENT,   // Chờ bệnh nhân thanh toán đơn thuốc
    WAITING_FOR_LAB,            // Chờ thanh toán phí xét nghiệm và thực hiện cận lâm sàng
    READY_FOR_REEXAM,           // Đã có kết quả xét nghiệm, sẵn sàng tái khám ưu tiên
    COMPLETED,                  // Đã hoàn tất quy trình khám bệnh
    CANCELLED                   // Đã hủy lịch khám
}
