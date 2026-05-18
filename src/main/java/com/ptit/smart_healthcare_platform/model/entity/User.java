package com.ptit.smart_healthcare_platform.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    // Mật khẩu được lưu dưới dạng BCrypt hash (CORE-01)
    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 100)
    private String fullName;

    @Column(unique = true, nullable = false, length = 15)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private com.ptit.smart_healthcare_platform.model.enums.UserStatus status = com.ptit.smart_healthcare_platform.model.enums.UserStatus.ACTIVE;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<UserRole> userRoles = new HashSet<>();

    // LOẠI BỎ quan hệ 1-1 hai chiều ngược (mappedBy) với Patient và Doctor để:
    // 1. Tối ưu hiệu năng, tránh truy vấn EAGER ngầm (N+1 query) khi tải danh sách User
    // 2. Khắc phục triệt để cảnh báo OneToOne từ IDE
}

