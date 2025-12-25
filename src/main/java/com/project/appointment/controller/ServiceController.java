package com.project.appointment.controller;

import com.project.appointment.dto.request.ServiceRequest;
import com.project.appointment.dto.response.ServiceResponse;
import com.project.appointment.security.JwtService;
import com.project.appointment.service.ServiceService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
public class ServiceController {
    
    private final ServiceService serviceService;
    private final JwtService jwtService;
    
    @PostMapping
    @PreAuthorize("hasRole('BUSINESS_OWNER')")
    public ResponseEntity<ServiceResponse> createService(
            @RequestParam Long businessId,
            @Valid @RequestBody ServiceRequest request,
            HttpServletRequest req) {
        Long ownerId = jwtService.getUserIdFromToken(jwtService.resolveToken(req));
        return ResponseEntity.status(HttpStatus.CREATED).body(serviceService.createService(businessId, request, ownerId));
    }
    
    @GetMapping
    public ResponseEntity<List<ServiceResponse>> getServicesByBusinessId(@RequestParam Long businessId) {
        return ResponseEntity.ok(serviceService.getServicesByBusinessId(businessId));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ServiceResponse> getServiceById(@PathVariable Long id) {
        return ResponseEntity.ok(serviceService.getServiceById(id));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('BUSINESS_OWNER')")
    public ResponseEntity<ServiceResponse> updateService(
            @PathVariable Long id,
            @Valid @RequestBody ServiceRequest request,
            HttpServletRequest req) {
        Long ownerId = jwtService.getUserIdFromToken(jwtService.resolveToken(req));
        return ResponseEntity.ok(serviceService.updateService(id, request, ownerId));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('BUSINESS_OWNER')")
    public ResponseEntity<Void> deleteService(@PathVariable Long id, HttpServletRequest req) {
        Long ownerId = jwtService.getUserIdFromToken(jwtService.resolveToken(req));
        serviceService.deleteService(id, ownerId);
        return ResponseEntity.noContent().build();
    }
}
