package com.project.appointment.controller;

import com.project.appointment.security.JwtService;
import com.project.appointment.service.AnalyticsService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {
    
    private final AnalyticsService analyticsService;
    private final JwtService jwtService;
    
    @GetMapping("/business/{businessId}")
    @PreAuthorize("hasRole('BUSINESS_OWNER')")
    public ResponseEntity<Map<String, Object>> getBusinessAnalytics(
            @PathVariable Long businessId,
            HttpServletRequest req) {
        Long ownerId = jwtService.getUserIdFromToken(jwtService.resolveToken(req));
        return ResponseEntity.ok(analyticsService.getBusinessAnalytics(businessId, ownerId));
    }
    
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<Map<String, Object>> getEmployeeAnalytics(@PathVariable Long employeeId) {
        return ResponseEntity.ok(analyticsService.getEmployeeAnalytics(employeeId));
    }
}
