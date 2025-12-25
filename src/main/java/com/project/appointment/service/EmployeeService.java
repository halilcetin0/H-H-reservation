package com.project.appointment.service;

import com.project.appointment.dto.request.EmployeeRequest;
import com.project.appointment.dto.response.EmployeeAnalyticsResponse;
import com.project.appointment.dto.response.EmployeeResponse;
import com.project.appointment.entity.Business;
import com.project.appointment.entity.Employee;
import com.project.appointment.exception.BusinessException;
import com.project.appointment.exception.ResourceNotFoundException;
import com.project.appointment.repository.AppointmentRepository;
import com.project.appointment.repository.BusinessRepository;
import com.project.appointment.repository.EmployeeRepository;
import com.project.appointment.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeService {
    
    private final EmployeeRepository employeeRepository;
    private final BusinessRepository businessRepository;
    private final AppointmentRepository appointmentRepository;
    private final ReviewRepository reviewRepository;
    
    @Transactional
    public EmployeeResponse createEmployee(Long businessId, EmployeeRequest request, Long ownerId) {
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new BusinessException("Business not found"));
        
        if (!business.getOwnerId().equals(ownerId)) {
            throw new BusinessException("You don't have permission to add employees to this business");
        }
        
        Employee employee = Employee.builder()
                .business(business)
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .build();
        
        employee = employeeRepository.save(employee);
        log.info("Employee created: {} for business: {}", employee.getId(), businessId);
        
        return mapToResponse(employee);
    }
    
    public List<EmployeeResponse> getEmployeesByBusinessId(Long businessId) {
        return employeeRepository.findByBusinessIdAndIsActiveTrue(businessId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    public EmployeeResponse getEmployeeById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        return mapToResponse(employee);
    }
    
    public EmployeeAnalyticsResponse getEmployeeAnalytics(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        
        BigDecimal totalEarnings = appointmentRepository.getTotalEarningsByEmployeeId(employeeId);
        Long totalAppointments = appointmentRepository.countByEmployeeId(employeeId);
        Double avgRating = reviewRepository.getAverageRatingByEmployeeId(employeeId);
        
        return EmployeeAnalyticsResponse.builder()
                .employeeId(employeeId)
                .employeeName(employee.getName())
                .totalEarnings(totalEarnings != null ? totalEarnings.doubleValue() : 0.0)
                .totalAppointments(totalAppointments != null ? totalAppointments : 0L)
                .averageRating(avgRating != null ? avgRating : 0.0)
                .build();
    }
    
    @Transactional
    public EmployeeResponse updateEmployee(Long employeeId, EmployeeRequest request, Long ownerId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        
        if (!employee.getBusiness().getOwnerId().equals(ownerId)) {
            throw new BusinessException("You don't have permission to update this employee");
        }
        
        if (request.getName() != null) employee.setName(request.getName());
        if (request.getEmail() != null) employee.setEmail(request.getEmail());
        if (request.getPhone() != null) employee.setPhone(request.getPhone());
        
        employee = employeeRepository.save(employee);
        log.info("Employee updated: {}", employee.getId());
        
        return mapToResponse(employee);
    }
    
    @Transactional
    public void deleteEmployee(Long employeeId, Long ownerId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        
        if (!employee.getBusiness().getOwnerId().equals(ownerId)) {
            throw new BusinessException("You don't have permission to delete this employee");
        }
        
        employee.setIsActive(false);
        employeeRepository.save(employee);
        log.info("Employee deleted (soft): {}", employeeId);
    }
    
    private EmployeeResponse mapToResponse(Employee employee) {
        return EmployeeResponse.builder()
                .id(employee.getId())
                .businessId(employee.getBusiness().getId())
                .name(employee.getName())
                .email(employee.getEmail())
                .phone(employee.getPhone())
                .isActive(employee.getIsActive())
                .createdAt(employee.getCreatedAt())
                .build();
    }
}
