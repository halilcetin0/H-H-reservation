package com.project.appointment.controller;

import com.project.appointment.dto.request.WorkScheduleRequest;
import com.project.appointment.dto.response.WorkScheduleResponse;
import com.project.appointment.security.JwtService;
import com.project.appointment.service.WorkScheduleService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/work-schedules")
@RequiredArgsConstructor
public class WorkScheduleController {
    
    private final WorkScheduleService workScheduleService;
    private final JwtService jwtService;
    
    @PostMapping
    @PreAuthorize("hasRole('BUSINESS_OWNER')")
    public ResponseEntity<WorkScheduleResponse> createWorkSchedule(
            @RequestParam Long employeeId,
            @Valid @RequestBody WorkScheduleRequest request,
            HttpServletRequest req) {
        Long ownerId = jwtService.getUserIdFromToken(jwtService.resolveToken(req));
        return ResponseEntity.status(HttpStatus.CREATED).body(workScheduleService.createWorkSchedule(employeeId, request, ownerId));
    }
    
    @GetMapping
    public ResponseEntity<List<WorkScheduleResponse>> getSchedulesByEmployeeId(@RequestParam Long employeeId) {
        return ResponseEntity.ok(workScheduleService.getSchedulesByEmployeeId(employeeId));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('BUSINESS_OWNER')")
    public ResponseEntity<WorkScheduleResponse> updateWorkSchedule(
            @PathVariable Long id,
            @Valid @RequestBody WorkScheduleRequest request,
            HttpServletRequest req) {
        Long ownerId = jwtService.getUserIdFromToken(jwtService.resolveToken(req));
        return ResponseEntity.ok(workScheduleService.updateWorkSchedule(id, request, ownerId));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('BUSINESS_OWNER')")
    public ResponseEntity<Void> deleteWorkSchedule(@PathVariable Long id, HttpServletRequest req) {
        Long ownerId = jwtService.getUserIdFromToken(jwtService.resolveToken(req));
        workScheduleService.deleteWorkSchedule(id, ownerId);
        return ResponseEntity.noContent().build();
    }
}
