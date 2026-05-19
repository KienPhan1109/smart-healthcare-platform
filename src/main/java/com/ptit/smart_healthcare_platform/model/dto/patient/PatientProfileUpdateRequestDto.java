package com.ptit.smart_healthcare_platform.model.dto.patient;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import com.ptit.smart_healthcare_platform.constant.ValidationConstants;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class PatientProfileUpdateRequestDto {

    @NotBlank(message = "Họ và tên không được để trống")
    private String fullName;

    @NotNull(message = "Ngày sinh không được để trống")
    @Past(message = "Ngày sinh phải là ngày trong quá khứ")
    private LocalDate dateOfBirth;

    @NotBlank(message = "Giới tính không được để trống")
    private String gender;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = ValidationConstants.PHONE_REGEX, message = "Số điện thoại phải bắt đầu bằng 0 và có đúng 10 chữ số")
    private String phoneNumber;

    private String medicalHistory;
    private String allergies;
    private String bloodType;

    @Positive(message = "Chiều cao phải là số dương")
    private BigDecimal height;

    @Positive(message = "Cân nặng phải là số dương")
    private BigDecimal weight;

    @Pattern(regexp = ValidationConstants.BHYT_REGEX, message = "Mã số thẻ BHYT phải đúng định dạng Việt Nam (2 chữ cái in hoa đầu và 13 chữ số tiếp theo, VD: GD4797918800001)")
    private String insuranceNumber;

    private LocalDate insuranceExpiryDate;
}
