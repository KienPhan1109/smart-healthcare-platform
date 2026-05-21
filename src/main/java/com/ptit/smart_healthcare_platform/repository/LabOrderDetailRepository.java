package com.ptit.smart_healthcare_platform.repository;

import com.ptit.smart_healthcare_platform.model.entity.LabOrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LabOrderDetailRepository extends JpaRepository<LabOrderDetail, Long> {
    List<LabOrderDetail> findAllByLabOrderId(Long labOrderId);
}
