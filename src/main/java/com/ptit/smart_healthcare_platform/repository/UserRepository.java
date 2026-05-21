package com.ptit.smart_healthcare_platform.repository;

import com.ptit.smart_healthcare_platform.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByPhoneNumber(String phoneNumber);

    Optional<User> findByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);

    boolean existsByEmail(String email);

    // Tai User kem toan bo Role (1 cau JOIN duy nhat, tranh N+1) - dung cho Spring Security
    @Query("SELECT u FROM User u JOIN FETCH u.userRoles ur JOIN FETCH ur.role WHERE u.phoneNumber = :phoneNumber")
    Optional<User> findByPhoneNumberWithRoles(String phoneNumber);

    @Query("SELECT DISTINCT u FROM User u " +
           "JOIN u.userRoles ur " +
           "LEFT JOIN Doctor d ON d.user.id = u.id " +
           "WHERE ur.role.name IN ('ROLE_DOCTOR', 'ROLE_TECHNICIAN') " +
           "AND (:keyword IS NULL OR :keyword = '' OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR u.phoneNumber LIKE CONCAT('%', :keyword, '%')) " +
           "AND (:specialtyId IS NULL OR (ur.role.name = 'ROLE_DOCTOR' AND d.specialty.id = :specialtyId))")
    Page<User> searchStaffs(@Param("keyword") String keyword, 
                            @Param("specialtyId") Long specialtyId, 
                            Pageable pageable);
}
