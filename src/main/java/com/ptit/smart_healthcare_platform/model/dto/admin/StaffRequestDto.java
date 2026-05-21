package com.ptit.smart_healthcare_platform.model.dto.admin;

import com.ptit.smart_healthcare_platform.model.enums.RoleName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class StaffRequestDto {

    private Long id; // For update if needed, but primarily creation

    @NotBlank(message = "Họ và tên không được để trống")
    private String fullName;

    @NotBlank(message = "Số điện thoại không được để trống")
    private String phoneNumber;

    private String email;

    @NotBlank(message = "Mật khẩu không được để trống")
    private String password;

    @NotNull(message = "Vui lòng chọn chức vụ")
    private RoleName roleName; // DOCTOR, TECHNICIAN, COORDINATOR

    // ----- Dành cho Bác sĩ (Role DOCTOR) -----
    private Long specialtyId;
    private String academicRank;
    private Integer experienceYears = 0;
    private BigDecimal examFee;
}
