package com.ptit.smart_healthcare_platform.model.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class AuthResponseDto {
    private Long userId;
    private String username;
    private String email;
    // Không bao giờ trả password hash về cho client
    private List<String> roles;
    private String message;
}
