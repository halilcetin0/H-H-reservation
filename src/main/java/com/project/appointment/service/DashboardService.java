package com.project.appointment.service;

import com.project.appointment.dto.response.DashboardResponse;
import com.project.appointment.entity.AppointmentStatus;
import com.project.appointment.exception.BusinessException;
import com.project.appointment.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {
    
    private final BusinessRepository businessRepository;
    private final AppointmentRepository appointmentRepository;
    private final ServiceRepository serviceRepository;
    private final EmployeeRepository employeeRepository;
    private final ReviewRepository reviewRepository;
    
    public DashboardResponse getBusinessDashboard(Long businessId, Long ownerId) {
        var business = businessRepository.findById(businessId)
                .orElseThrow(() -> new BusinessException("Business not found"));
        
        if (!business.getOwnerId().equals(ownerId)) {
            throw new BusinessException("You don't have permission to view this dashboard");
        }
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayStart = now.toLocalDate().atStartOfDay();
        LocalDateTime todayEnd = todayStart.plusDays(1);
        LocalDateTime weekEnd = now.plusDays(7);
        LocalDateTime monthStart = now.withDayOfMonth(1).toLocalDate().atStartOfDay();
        
        Long totalAppointments = appointmentRepository.countByBusinessId(businessId);
        Long todayAppointments = appointmentRepository.countByBusinessIdAndAppointmentTimeBetween(businessId, todayStart, todayEnd);
        Long upcomingAppointments = appointmentRepository.countByBusinessIdAndAppointmentTimeBetween(businessId, now, weekEnd);
        Long completedAppointments = appointmentRepository.countByBusinessIdAndStatus(businessId, AppointmentStatus.COMPLETED);
        Long cancelledAppointments = appointmentRepository.countByBusinessIdAndStatus(businessId, AppointmentStatus.CANCELLED);
        
        BigDecimal totalRevenueBD = appointmentRepository.getTotalRevenueByBusinessId(businessId);
        BigDecimal monthlyRevenueBD = appointmentRepository.getTotalRevenueByBusinessIdAndDateRange(businessId, monthStart, now);
        
        Double totalRevenue = totalRevenueBD != null ? totalRevenueBD.doubleValue() : 0.0;
        Double monthlyRevenue = monthlyRevenueBD != null ? monthlyRevenueBD.doubleValue() : 0.0;
        Double averageRating = reviewRepository.getAverageRatingByBusinessId(businessId);
        
        // Appointments by status
        Map<String, Long> appointmentsByStatus = new HashMap<>();
        for (AppointmentStatus status : AppointmentStatus.values()) {
            Long count = appointmentRepository.countByBusinessIdAndStatus(businessId, status);
            appointmentsByStatus.put(status.name(), count != null ? count : 0L);
        }
        
        // Last 7 days appointments
        Map<String, Long> last7DaysAppointments = new LinkedHashMap<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = dayStart.plusDays(1);
            Long count = appointmentRepository.countByBusinessIdAndAppointmentTimeBetween(businessId, dayStart, dayEnd);
            last7DaysAppointments.put(date.toString(), count != null ? count : 0L);
        }
        
        // Top 5 services
        List<Map<String, Object>> topServices = serviceRepository.findByBusinessIdAndIsActiveTrue(businessId)
                .stream()
                .map(service -> {
                    Long appointmentCount = appointmentRepository.countByServiceId(service.getId());
                    Map<String, Object> serviceData = new HashMap<>();
                    serviceData.put("name", service.getName());
                    serviceData.put("appointments", appointmentCount != null ? appointmentCount : 0L);
                    serviceData.put("revenue", service.getPrice().multiply(BigDecimal.valueOf(appointmentCount != null ? appointmentCount : 0)));
                    return serviceData;
                })
                .sorted((a, b) -> ((Long) b.get("appointments")).compareTo((Long) a.get("appointments")))
                .limit(5)
                .collect(Collectors.toList());
        
        // Top 5 employees
        List<Map<String, Object>> topEmployees = employeeRepository.findByBusinessIdAndIsActiveTrue(businessId)
                .stream()
                .map(employee -> {
                    Long appointmentCount = appointmentRepository.countByEmployeeId(employee.getId());
                    BigDecimal earnings = appointmentRepository.getTotalEarningsByEmployeeId(employee.getId());
                    Double rating = reviewRepository.getAverageRatingByEmployeeId(employee.getId());
                    
                    Map<String, Object> employeeData = new HashMap<>();
                    employeeData.put("name", employee.getName());
                    employeeData.put("appointments", appointmentCount != null ? appointmentCount : 0L);
                    employeeData.put("earnings", earnings != null ? earnings.doubleValue() : 0.0);
                    employeeData.put("rating", rating != null ? rating : 0.0);
                    return employeeData;
                })
                .sorted((a, b) -> ((Long) b.get("appointments")).compareTo((Long) a.get("appointments")))
                .limit(5)
                .collect(Collectors.toList());
        
        return DashboardResponse.builder()
                .totalAppointments(totalAppointments != null ? totalAppointments : 0L)
                .todayAppointments(todayAppointments != null ? todayAppointments : 0L)
                .upcomingAppointments(upcomingAppointments != null ? upcomingAppointments : 0L)
                .completedAppointments(completedAppointments != null ? completedAppointments : 0L)
                .cancelledAppointments(cancelledAppointments != null ? cancelledAppointments : 0L)
                .totalRevenue(totalRevenue)
                .monthlyRevenue(monthlyRevenue)
                .averageRating(averageRating != null ? averageRating : 0.0)
                .appointmentsByStatus(appointmentsByStatus)
                .last7DaysAppointments(last7DaysAppointments)
                .topServices(topServices)
                .topEmployees(topEmployees)
                .build();
    }
}
