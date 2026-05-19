package com.ptit.smart_healthcare_platform.service;

import com.ptit.smart_healthcare_platform.model.entity.User;
import com.ptit.smart_healthcare_platform.model.enums.UserStatus;
import com.ptit.smart_healthcare_platform.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Spring Security goi phuong thuc nay voi tham so la gia tri tu truong usernameParameter
    // Trong SecurityConfig da cau hinh usernameParameter = "phoneNumber"
    // Nen tham so "username" o day thuc te la so dien thoai
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String phoneNumber) throws UsernameNotFoundException {
        User user = userRepository.findByPhoneNumberWithRoles(phoneNumber)
                .orElseThrow(() -> new UsernameNotFoundException("Khong tim thay tai khoan voi SĐT: " + phoneNumber));

        // Chan dang nhap neu tai khoan bi khoa
        boolean isEnabled = user.getStatus() == UserStatus.ACTIVE;

        Set<GrantedAuthority> authorities = user.getUserRoles().stream()
                .map(ur -> new SimpleGrantedAuthority(ur.getRole().getName().name()))
                .collect(Collectors.toSet());

        return new org.springframework.security.core.userdetails.User(
                user.getPhoneNumber(),  // Dung phoneNumber lam principal
                user.getPassword(),
                isEnabled,
                true,
                true,
                true,
                authorities
        );
    }
}
