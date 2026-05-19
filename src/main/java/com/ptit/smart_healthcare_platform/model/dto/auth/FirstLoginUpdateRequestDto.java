package com.ptit.smart_healthcare_platform.model.dto.auth;

import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FirstLoginUpdateRequestDto {

    @Pattern(regexp = "^$|^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "Địa chỉ Email không hợp lệ (Ví dụ: abc@domain.com)")
    private String email;

    private String dateOfBirth;

    private String gender;

    @Pattern(regexp = "^$|^[0-9]{12}$", message = "Số CCCD phải đúng 12 chữ số")
    private String identityCard;

    @Pattern(regexp = "^$|^[a-zA-Z0-9]{15}$", message = "Số thẻ BHYT phải đúng 15 ký tự")
    private String insuranceNumber;
}
