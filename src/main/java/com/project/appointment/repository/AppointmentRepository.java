package com.project.appointment.repository;

import com.project.appointment.entity.Appointment;
import com.project.appointment.entity.AppointmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long>, JpaSpecificationExecutor<Appointment> {
    
    @Query("SELECT a FROM Appointment a WHERE a.customer.id = :userId")
    List<Appointment> findByUserId(@Param("userId") Long userId);
    
    Page<Appointment> findByBusinessId(Long businessId, Pageable pageable);
    
    Long countByBusinessId(Long businessId);
    
    Long countByBusinessIdAndStatus(Long businessId, AppointmentStatus status);
    
    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.business.id = :businessId " +
           "AND a.startTime >= :start AND a.startTime < :end")
    Long countByBusinessIdAndAppointmentTimeBetween(
            @Param("businessId") Long businessId, 
            @Param("start") LocalDateTime start, 
            @Param("end") LocalDateTime end);
    
    Long countByEmployeeId(Long employeeId);
    
    Long countByServiceId(Long serviceId);
    
    @Query("SELECT COALESCE(SUM(a.price), 0) FROM Appointment a " +
           "WHERE a.business.id = :businessId AND a.paymentStatus = 'PAID'")
    BigDecimal getTotalRevenueByBusinessId(@Param("businessId") Long businessId);
    
    @Query("SELECT COALESCE(SUM(a.price), 0) FROM Appointment a " +
           "WHERE a.business.id = :businessId AND a.paymentStatus = 'PAID' " +
           "AND a.startTime >= :start AND a.startTime < :end")
    BigDecimal getTotalRevenueByBusinessIdAndDateRange(
            @Param("businessId") Long businessId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
    
    @Query("SELECT COALESCE(SUM(a.price), 0) FROM Appointment a " +
           "WHERE a.employee.id = :employeeId AND a.paymentStatus = 'PAID'")
    BigDecimal getTotalEarningsByEmployeeId(@Param("employeeId") Long employeeId);
    
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM Appointment a " +
           "WHERE a.employee.id = :employeeId " +
           "AND a.startTime < :endTime AND a.endTime > :startTime " +
           "AND a.status != :excludeStatus")
    boolean existsByEmployeeIdAndAppointmentTimeBetweenAndStatusNot(
            @Param("employeeId") Long employeeId, 
            @Param("startTime") LocalDateTime startTime, 
            @Param("endTime") LocalDateTime endTime, 
            @Param("excludeStatus") AppointmentStatus excludeStatus);
    
    @Query("SELECT a FROM Appointment a " +
           "WHERE a.employee.id = :employeeId " +
           "AND a.startTime >= :startTime AND a.startTime < :endTime " +
           "AND a.status != :excludeStatus")
    List<Appointment> findByEmployeeIdAndAppointmentTimeBetweenAndStatusNot(
            @Param("employeeId") Long employeeId, 
            @Param("startTime") LocalDateTime startTime, 
            @Param("endTime") LocalDateTime endTime, 
            @Param("excludeStatus") AppointmentStatus excludeStatus);
    
    @Query("SELECT a FROM Appointment a " +
           "WHERE a.reminderSent = false " +
           "AND a.startTime >= :start AND a.startTime <= :end " +
           "AND a.status = 'CONFIRMED'")
    List<Appointment> findByReminderSentFalseAndAppointmentTimeBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
}
