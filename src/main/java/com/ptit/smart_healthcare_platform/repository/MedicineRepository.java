package com.ptit.smart_healthcare_platform.repository;

import com.ptit.smart_healthcare_platform.model.entity.Medicine;
import com.ptit.smart_healthcare_platform.model.enums.MedicineStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicineRepository extends JpaRepository<Medicine, Long> {
    List<Medicine> findAllByStatus(MedicineStatus status);
    List<Medicine> findAllByStatusAndStockQuantityGreaterThan(MedicineStatus status, Integer stockQuantity);
    
    Page<Medicine> findByNameContainingIgnoreCase(String keyword, Pageable pageable);
}
