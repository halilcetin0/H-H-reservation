package com.project.appointment.service;

import com.project.appointment.dto.request.WorkScheduleRequest;
import com.project.appointment.dto.response.WorkScheduleResponse;
import com.project.appointment.entity.Employee;
import com.project.appointment.entity.WorkSchedule;
import com.project.appointment.exception.BusinessException;
import com.project.appointment.exception.ResourceNotFoundException;
import com.project.appointment.repository.EmployeeRepository;
import com.project.appointment.repository.WorkScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkScheduleService {
    
    private final WorkScheduleRepository workScheduleRepository;
    private final EmployeeRepository employeeRepository;
    
    @Transactional
    public WorkScheduleResponse createWorkSchedule(Long employeeId, WorkScheduleRequest request, Long ownerId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        
        if (!employee.getBusiness().getOwnerId().equals(ownerId)) {
            throw new BusinessException("You don't have permission to add schedules for this employee");
        }
        
        if (workScheduleRepository.existsByEmployeeIdAndDayOfWeek(employeeId, request.getDayOfWeek())) {
            throw new BusinessException("Schedule already exists for " + request.getDayOfWeek());
        }
        
        WorkSchedule schedule = WorkSchedule.builder()
                .employee(employee)
                .dayOfWeek(request.getDayOfWeek())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .build();
        
        schedule = workScheduleRepository.save(schedule);
        log.info("Work schedule created: {} for employee: {}", schedule.getId(), employeeId);
        
        return mapToResponse(schedule);
    }
    
    public List<WorkScheduleResponse> getSchedulesByEmployeeId(Long employeeId) {
        return workScheduleRepository.findByEmployeeId(employeeId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public WorkScheduleResponse updateWorkSchedule(Long scheduleId, WorkScheduleRequest request, Long ownerId) {
        WorkSchedule schedule = workScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Work schedule not found"));
        
        if (!schedule.getEmployee().getBusiness().getOwnerId().equals(ownerId)) {
            throw new BusinessException("You don't have permission to update this schedule");
        }
        
        if (request.getDayOfWeek() != null) schedule.setDayOfWeek(request.getDayOfWeek());
        if (request.getStartTime() != null) schedule.setStartTime(request.getStartTime());
        if (request.getEndTime() != null) schedule.setEndTime(request.getEndTime());
        
        schedule = workScheduleRepository.save(schedule);
        log.info("Work schedule updated: {}", schedule.getId());
        
        return mapToResponse(schedule);
    }
    
    @Transactional
    public void deleteWorkSchedule(Long scheduleId, Long ownerId) {
        WorkSchedule schedule = workScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Work schedule not found"));
        
        if (!schedule.getEmployee().getBusiness().getOwnerId().equals(ownerId)) {
            throw new BusinessException("You don't have permission to delete this schedule");
        }
        
        workScheduleRepository.delete(schedule);
        log.info("Work schedule deleted: {}", scheduleId);
    }
    
    private WorkScheduleResponse mapToResponse(WorkSchedule schedule) {
        return WorkScheduleResponse.builder()
                .id(schedule.getId())
                .employeeId(schedule.getEmployee().getId())
                .dayOfWeek(schedule.getDayOfWeek())
                .startTime(schedule.getStartTime())
                .endTime(schedule.getEndTime())
                .isActive(schedule.getIsActive())
                .build();
    }
}
