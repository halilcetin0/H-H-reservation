package com.project.appointment.controller;

import com.project.appointment.dto.request.EmployeeRequest;
import com.project.appointment.dto.response.ApiResponse;
import com.project.appointment.dto.response.EmployeeAnalyticsResponse;
import com.project.appointment.dto.response.EmployeeResponse;
import com.project.appointment.security.JwtService;
import com.project.appointment.service.EmployeeService;
import com.project.appointment.service.StaffInvitationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {
    
    private final EmployeeService employeeService;
    private final StaffInvitationService invitationService;
    private final JwtService jwtService;
    
    @PostMapping
    @PreAuthorize("hasRole('BUSINESS_OWNER')")
    public ResponseEntity<EmployeeResponse> createEmployee(
            @RequestParam Long businessId,
            @Valid @RequestBody EmployeeRequest request,
            HttpServletRequest req) {
        Long ownerId = jwtService.getUserIdFromToken(jwtService.resolveToken(req));
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeService.createEmployee(businessId, request, ownerId));
    }
    
    @GetMapping
    public ResponseEntity<List<EmployeeResponse>> getEmployeesByBusinessId(@RequestParam Long businessId) {
        return ResponseEntity.ok(employeeService.getEmployeesByBusinessId(businessId));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponse> getEmployeeById(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.getEmployeeById(id));
    }
    
    @GetMapping("/{id}/analytics")
    public ResponseEntity<EmployeeAnalyticsResponse> getEmployeeAnalytics(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.getEmployeeAnalytics(id));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('BUSINESS_OWNER')")
    public ResponseEntity<EmployeeResponse> updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeRequest request,
            HttpServletRequest req) {
        Long ownerId = jwtService.getUserIdFromToken(jwtService.resolveToken(req));
        return ResponseEntity.ok(employeeService.updateEmployee(id, request, ownerId));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('BUSINESS_OWNER')")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id, HttpServletRequest req) {
        Long ownerId = jwtService.getUserIdFromToken(jwtService.resolveToken(req));
        employeeService.deleteEmployee(id, ownerId);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/accept-invitation")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Object>> acceptInvitation(
            @RequestParam String token,
            HttpServletRequest req) {
        Long userId = jwtService.getUserIdFromToken(jwtService.resolveToken(req));
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Kullanıcı kimlik doğrulaması yapılmadı"));
        }
        invitationService.acceptInvitation(token, userId);
        return ResponseEntity.ok(ApiResponse.success(null, "Davetiye başarıyla kabul edildi. Artık bu işletmede çalışan olarak görev yapabilirsiniz."));
    }
}
