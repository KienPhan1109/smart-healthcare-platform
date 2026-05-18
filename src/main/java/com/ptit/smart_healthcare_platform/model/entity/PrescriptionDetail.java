package com.ptit.smart_healthcare_platform.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "prescription_details")
@Getter
@Setter
public class PrescriptionDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prescription_id", nullable = false)
    private Prescription prescription;

    // Quan hệ N-1 với Medicine (nhiều chi tiết đơn thuốc tham chiếu cùng 1 loại thuốc)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicine_id", nullable = false)
    private Medicine medicine;

    // Số lượng thuốc kê cho bệnh nhân
    @Column(nullable = false)
    private Integer quantity;

    // Liều dùng (VD: "500mg", "1 viên")
    @Column(nullable = false, length = 100)
    private String dosage;

    // Tần suất sử dụng (VD: "2 lần/ngày sau ăn", "Sáng - Tối")
    @Column(nullable = false, length = 200)
    private String frequency;

    // Hướng dẫn đặc biệt cho loại thuốc này
    @Column(columnDefinition = "TEXT")
    private String note;
}
