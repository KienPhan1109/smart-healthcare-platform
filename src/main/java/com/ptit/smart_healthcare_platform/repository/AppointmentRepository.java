package com.ptit.smart_healthcare_platform.repository;

import com.ptit.smart_healthcare_platform.model.entity.Appointment;
import com.ptit.smart_healthcare_platform.model.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    
    List<Appointment> findAllByPatientUserIdAndIsDeletedFalseOrderByAppointmentTimeDesc(Long userId);
    
    boolean existsByDoctorIdAndAppointmentTimeAndIsDeletedFalse(Long doctorId, LocalDateTime appointmentTime);
    
    List<Appointment> findAllByStatusAndCreatedAtBeforeAndIsDeletedFalse(AppointmentStatus status, LocalDateTime timeLimit);
    
    boolean existsByPatientIdAndIsDeletedFalse(Long patientId);
}
