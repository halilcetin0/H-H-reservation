package com.project.appointment.repository;

import com.project.appointment.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    
    List<Employee> findByBusinessIdAndIsActiveTrue(Long businessId);
    
    List<Employee> findByBusinessId(Long businessId);
    
    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.employee.id = :employeeId")
    Long countAppointmentsByEmployeeId(@Param("employeeId") Long employeeId);
    
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.employee.id = :employeeId")
    Double getAverageRatingByEmployeeId(@Param("employeeId") Long employeeId);
    
    @Query("SELECT COALESCE(SUM(a.price), 0) FROM Appointment a " +
           "WHERE a.employee.id = :employeeId AND a.paymentStatus = 'PAID'")
    Double getTotalEarningsByEmployeeId(@Param("employeeId") Long employeeId);
}

