package com.ptit.smart_healthcare_platform.model.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import com.ptit.smart_healthcare_platform.constant.ValidationConstants;

@Getter
@Setter
public class RegisterRequestDto {

    @NotBlank(message = "Tên đăng nhập không được để trống")
    @Size(min = 4, max = 50, message = "Tên đăng nhập phải từ 4-50 ký tự")
    private String username;

    @NotBlank(message = "Email không được để trống")
    @Pattern(regexp = ValidationConstants.EMAIL_REGEX, message = "Email không đúng định dạng (VD: kien@gmail.com)")
    @Size(max = 100, message = "Email không được vượt quá 100 ký tự")
    private String email;

    // Mật khẩu sẽ được BCrypt hash tại Service layer trước khi lưu DB
    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, max = 100, message = "Mật khẩu phải từ 6-100 ký tự")
    private String password;

    @NotBlank(message = "Họ và tên không được để trống")
    @Size(max = 100, message = "Họ và tên không được vượt quá 100 ký tự")
    private String fullName;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = ValidationConstants.PHONE_REGEX, message = "Số điện thoại phải bắt đầu bằng 0 và có đúng 10 chữ số")
    private String phoneNumber;
}
