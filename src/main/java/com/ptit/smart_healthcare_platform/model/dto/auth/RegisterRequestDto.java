package com.ptit.smart_healthcare_platform.model.dto.auth;

import com.ptit.smart_healthcare_platform.constant.ValidationConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequestDto {

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = ValidationConstants.PHONE_REGEX, message = "Số điện thoại phải bắt đầu bằng số 0 và có đúng 10 chữ số")
    private String phoneNumber;

    @NotBlank(message = "Họ và tên không được để trống")
    @Size(max = 100, message = "Họ và tên không được vượt quá 100 ký tự")
    private String fullName;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, max = 100, message = "Mật khẩu phải có tối thiểu 6 ký tự")
    private String password;

    @NotBlank(message = "Xác nhận mật khẩu không được để trống")
    private String confirmPassword;
}
