package com.project.appointment.controller;

import com.project.appointment.dto.request.BusinessRequest;
import com.project.appointment.dto.response.BusinessResponse;
import com.project.appointment.security.JwtService;
import com.project.appointment.service.BusinessService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/businesses")
@RequiredArgsConstructor
public class BusinessController {
    
    private final BusinessService businessService;
    private final JwtService jwtService;
    
    @PostMapping
    @PreAuthorize("hasRole('BUSINESS_OWNER')")
    public ResponseEntity<BusinessResponse> createBusiness(
            @Valid @RequestBody BusinessRequest request,
            HttpServletRequest req) {
        Long ownerId = jwtService.getUserIdFromToken(jwtService.resolveToken(req));
        return ResponseEntity.status(HttpStatus.CREATED).body(businessService.createBusiness(ownerId, request));
    }
    
    @GetMapping
    public ResponseEntity<Page<BusinessResponse>> getAllBusinesses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(businessService.getAllBusinesses(PageRequest.of(page, size, Sort.by("name"))));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<BusinessResponse> getBusinessById(@PathVariable Long id) {
        return ResponseEntity.ok(businessService.getBusinessById(id));
    }
    
    @GetMapping("/my")
    @PreAuthorize("hasRole('BUSINESS_OWNER')")
    public ResponseEntity<BusinessResponse> getMyBusiness(HttpServletRequest req) {
        Long ownerId = jwtService.getUserIdFromToken(jwtService.resolveToken(req));
        return ResponseEntity.ok(businessService.getMyBusiness(ownerId));
    }
    
    @PutMapping("/my")
    @PreAuthorize("hasRole('BUSINESS_OWNER')")
    public ResponseEntity<BusinessResponse> updateBusiness(
            @Valid @RequestBody BusinessRequest request,
            HttpServletRequest req) {
        Long ownerId = jwtService.getUserIdFromToken(jwtService.resolveToken(req));
        return ResponseEntity.ok(businessService.updateBusiness(ownerId, request));
    }
    
    @GetMapping("/search")
    public ResponseEntity<Page<BusinessResponse>> searchBusinesses(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(businessService.searchBusinesses(keyword, PageRequest.of(page, size)));
    }
}
