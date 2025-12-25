package com.project.appointment.service;

import com.project.appointment.exception.BusinessException;
import com.project.appointment.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {
    
    private final BusinessRepository businessRepository;
    private final AppointmentRepository appointmentRepository;
    private final ReviewRepository reviewRepository;
    
    public Map<String, Object> getBusinessAnalytics(Long businessId, Long ownerId) {
        // Verify ownership
        var business = businessRepository.findById(businessId)
                .orElseThrow(() -> new BusinessException("Business not found"));
        
        if (!business.getOwnerId().equals(ownerId)) {
            throw new BusinessException("You don't have permission to view analytics for this business");
        }
        
        Map<String, Object> analytics = new HashMap<>();
        
        // Total appointments
        Long totalAppointments = appointmentRepository.countByBusinessId(businessId);
        analytics.put("totalAppointments", totalAppointments);
        
        // Total revenue
        BigDecimal totalRevenue = appointmentRepository.getTotalRevenueByBusinessId(businessId);
        analytics.put("totalRevenue", totalRevenue != null ? totalRevenue : BigDecimal.ZERO);
        
        // Average rating
        Double avgRating = reviewRepository.getAverageRatingByBusinessId(businessId);
        analytics.put("averageRating", avgRating != null ? avgRating : 0.0);
        
        // Total reviews
        Long totalReviews = reviewRepository.countByBusinessId(businessId);
        analytics.put("totalReviews", totalReviews);
        
        return analytics;
    }
    
    public Map<String, Object> getEmployeeAnalytics(Long employeeId) {
        Map<String, Object> analytics = new HashMap<>();
        
        // Total appointments
        Long totalAppointments = appointmentRepository.countByEmployeeId(employeeId);
        analytics.put("totalAppointments", totalAppointments);
        
        // Total earnings
        BigDecimal totalEarnings = appointmentRepository.getTotalEarningsByEmployeeId(employeeId);
        analytics.put("totalEarnings", totalEarnings != null ? totalEarnings : BigDecimal.ZERO);
        
        // Average rating
        Double avgRating = reviewRepository.getAverageRatingByEmployeeId(employeeId);
        analytics.put("averageRating", avgRating != null ? avgRating : 0.0);
        
        return analytics;
    }
}

