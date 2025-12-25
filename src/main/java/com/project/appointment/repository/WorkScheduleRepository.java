package com.project.appointment.repository;

import com.project.appointment.entity.DayOfWeek;
import com.project.appointment.entity.WorkSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkScheduleRepository extends JpaRepository<WorkSchedule, Long> {
    
    List<WorkSchedule> findByEmployeeId(Long employeeId);
    
    Optional<WorkSchedule> findByEmployeeIdAndDayOfWeek(Long employeeId, DayOfWeek dayOfWeek);
    
    boolean existsByEmployeeIdAndDayOfWeek(Long employeeId, DayOfWeek dayOfWeek);
}

