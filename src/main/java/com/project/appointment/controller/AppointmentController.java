package com.project.appointment.controller;

import com.project.appointment.dto.request.AppointmentRequest;
import com.project.appointment.dto.request.AppointmentSearchRequest;
import com.project.appointment.dto.response.AppointmentResponse;
import com.project.appointment.dto.response.AvailableSlotResponse;
import com.project.appointment.entity.AppointmentStatus;
import com.project.appointment.security.JwtService;
import com.project.appointment.service.AppointmentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {
    
    private final AppointmentService appointmentService;
    private final JwtService jwtService;
    
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AppointmentResponse> createAppointment(
            @Valid @RequestBody AppointmentRequest request,
            HttpServletRequest req) {
        Long userId = jwtService.getUserIdFromToken(jwtService.resolveToken(req));
        return ResponseEntity.status(HttpStatus.CREATED).body(appointmentService.createAppointment(request, userId));
    }
    
    @PutMapping("/{id}/status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AppointmentResponse> updateAppointmentStatus(
            @PathVariable Long id,
            @RequestParam AppointmentStatus status,
            HttpServletRequest req) {
        Long userId = jwtService.getUserIdFromToken(jwtService.resolveToken(req));
        return ResponseEntity.ok(appointmentService.updateAppointmentStatus(id, status, userId));
    }
    
    @PostMapping("/{id}/approve/owner")
    @PreAuthorize("hasRole('BUSINESS_OWNER')")
    public ResponseEntity<AppointmentResponse> approveByOwner(
            @PathVariable Long id,
            HttpServletRequest req) {
        Long userId = jwtService.getUserIdFromToken(jwtService.resolveToken(req));
        return ResponseEntity.ok(appointmentService.approveAppointmentByOwner(id, userId));
    }
    
    @PostMapping("/{id}/approve/employee")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<AppointmentResponse> approveByEmployee(
            @PathVariable Long id,
            HttpServletRequest req) {
        Long userId = jwtService.getUserIdFromToken(jwtService.resolveToken(req));
        return ResponseEntity.ok(appointmentService.approveAppointmentByEmployee(id, userId));
    }
    
    @PostMapping("/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<AppointmentResponse>> searchAppointments(
            @Valid @RequestBody AppointmentSearchRequest search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(appointmentService.searchAppointments(search, PageRequest.of(page, size)));
    }
    
    @GetMapping("/available-slots")
    public ResponseEntity<List<AvailableSlotResponse>> getAvailableSlots(
            @RequestParam Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam Integer duration) {
        return ResponseEntity.ok(appointmentService.getAvailableSlots(employeeId, date, duration));
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AppointmentResponse> getAppointmentById(@PathVariable Long id, HttpServletRequest req) {
        Long userId = jwtService.getUserIdFromToken(jwtService.resolveToken(req));
        return ResponseEntity.ok(appointmentService.getAppointmentById(id, userId));
    }
    
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAppointments(
            @RequestParam(required = false) Long businessId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest req) {
        Long userId = jwtService.getUserIdFromToken(jwtService.resolveToken(req));
        
        // If businessId is provided and user is business owner, return business appointments
        if (businessId != null) {
            try {
                return ResponseEntity.ok(appointmentService.getBusinessAppointments(businessId, userId, PageRequest.of(page, size)));
            } catch (Exception e) {
                // If user is not the owner, fall through to return user appointments
            }
        }
        
        // Return user's appointments as a page
        List<AppointmentResponse> userAppointments = appointmentService.getUserAppointments(userId);
        int start = page * size;
        int end = Math.min(start + size, userAppointments.size());
        List<AppointmentResponse> pagedAppointments = start < userAppointments.size() 
            ? userAppointments.subList(start, end) 
            : new ArrayList<>();
        
        Page<AppointmentResponse> appointmentPage = new org.springframework.data.domain.PageImpl<>(
            pagedAppointments,
            PageRequest.of(page, size),
            userAppointments.size()
        );
        
        return ResponseEntity.ok(appointmentPage);
    }
    
    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<AppointmentResponse>> getMyAppointments(HttpServletRequest req) {
        Long userId = jwtService.getUserIdFromToken(jwtService.resolveToken(req));
        return ResponseEntity.ok(appointmentService.getUserAppointments(userId));
    }
    
    @GetMapping("/business/{businessId}")
    @PreAuthorize("hasRole('BUSINESS_OWNER')")
    public ResponseEntity<Page<AppointmentResponse>> getBusinessAppointments(
            @PathVariable Long businessId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest req) {
        Long ownerId = jwtService.getUserIdFromToken(jwtService.resolveToken(req));
        return ResponseEntity.ok(appointmentService.getBusinessAppointments(businessId, ownerId, PageRequest.of(page, size)));
    }
}
