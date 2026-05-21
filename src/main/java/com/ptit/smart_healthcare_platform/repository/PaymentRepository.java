package com.ptit.smart_healthcare_platform.repository;

import com.ptit.smart_healthcare_platform.model.entity.Payment;
import com.ptit.smart_healthcare_platform.model.enums.PaymentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByAppointmentIdAndTypeAndIsDeletedFalse(Long appointmentId, PaymentType type);

    @Query("SELECT MONTH(p.createdAt) as month, SUM(p.amount) as revenue " +
           "FROM Payment p " +
           "WHERE p.status = 'PAID' AND YEAR(p.createdAt) = :year " +
           "GROUP BY MONTH(p.createdAt) " +
           "ORDER BY month")
    List<Object[]> getMonthlyRevenueByYear(@Param("year") int year);
}
