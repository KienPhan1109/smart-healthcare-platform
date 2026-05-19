package com.ptit.smart_healthcare_platform.model.dto.auth;

import com.ptit.smart_healthcare_platform.constant.ValidationConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ForgotPasswordStep1Dto {

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = ValidationConstants.PHONE_REGEX, message = "Số điện thoại phải bắt đầu bằng số 0 và có đúng 10 chữ số")
    private String phoneNumber;
}
