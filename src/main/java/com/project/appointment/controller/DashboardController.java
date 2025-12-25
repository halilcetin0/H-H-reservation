package com.project.appointment.controller;

import com.project.appointment.dto.response.DashboardResponse;
import com.project.appointment.security.JwtService;
import com.project.appointment.service.DashboardService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    
    private final DashboardService dashboardService;
    private final JwtService jwtService;
    
    @GetMapping("/business/{businessId}")
    @PreAuthorize("hasRole('BUSINESS_OWNER')")
    public ResponseEntity<DashboardResponse> getBusinessDashboard(
            @PathVariable Long businessId,
            HttpServletRequest req) {
        Long ownerId = jwtService.getUserIdFromToken(jwtService.resolveToken(req));
        return ResponseEntity.ok(dashboardService.getBusinessDashboard(businessId, ownerId));
    }
}
