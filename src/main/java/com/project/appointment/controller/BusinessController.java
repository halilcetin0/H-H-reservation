package com.project.appointment.controller;

import com.project.appointment.dto.request.*;
import com.project.appointment.dto.response.*;
import com.project.appointment.entity.StaffInvitation;
import com.project.appointment.exception.BusinessException;
import com.project.appointment.security.JwtService;
import com.project.appointment.service.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/businesses")
@RequiredArgsConstructor
@Slf4j
public class BusinessController {
    
    private final BusinessService businessService;
    private final ServiceService serviceService;
    private final EmployeeService employeeService;
    private final WorkScheduleService workScheduleService;
    private final StaffInvitationService staffInvitationService;
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
    public ResponseEntity<ApiResponse<BusinessResponse>> getBusinessById(@PathVariable Long id) {
        BusinessResponse business = businessService.getBusinessById(id);
        return ResponseEntity.ok(ApiResponse.success(business, "İşletme bilgileri başarıyla getirildi"));
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
    
    @GetMapping("/my-business")
    @PreAuthorize("hasRole('BUSINESS_OWNER')")
    public ResponseEntity<ApiResponse<BusinessResponse>> getMyBusinessApi(HttpServletRequest req) {
        Long ownerId = jwtService.getUserIdFromToken(jwtService.resolveToken(req));
        try {
            BusinessResponse business = businessService.getMyBusiness(ownerId);
            return ResponseEntity.ok(ApiResponse.success(business, "İşletme bilgileri başarıyla getirildi"));
        } catch (BusinessException e) {
            if (e.getMessage() != null && e.getMessage().contains("don't have a business yet")) {
                // İşletme yoksa özel response döndür
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("İşletme bulunamadı. Lütfen önce işletme bilgilerinizi oluşturun."));
            }
            throw e;
        }
    }
    
    @GetMapping("/me")
    @PreAuthorize("hasRole('BUSINESS_OWNER')")
    public ResponseEntity<ApiResponse<BusinessResponse>> getMyBusinessMe(HttpServletRequest req) {
        Long ownerId = jwtService.getUserIdFromToken(jwtService.resolveToken(req));
        try {
            BusinessResponse business = businessService.getMyBusiness(ownerId);
            return ResponseEntity.ok(ApiResponse.success(business, "İşletme bilgileri başarıyla getirildi"));
        } catch (BusinessException e) {
            if (e.getMessage() != null && e.getMessage().contains("don't have a business yet")) {
                // İşletme yoksa özel response döndür
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("İşletme bulunamadı. Lütfen önce işletme bilgilerinizi oluşturun."));
            }
            throw e;
        }
    }
    
    @PutMapping("/{businessId}")
    @PreAuthorize("hasRole('BUSINESS_OWNER')")
    public ResponseEntity<ApiResponse<BusinessResponse>> updateBusinessById(
            @PathVariable Long businessId,
            @Valid @RequestBody BusinessRequest request,
            HttpServletRequest req) {
        Long ownerId = jwtService.getUserIdFromToken(jwtService.resolveToken(req));
        BusinessResponse business = businessService.getBusinessById(businessId);
        if (!business.getOwnerId().equals(ownerId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Bu işletmeye erişim yetkiniz yok"));
        }
        BusinessResponse updated = businessService.updateBusiness(ownerId, request);
        return ResponseEntity.ok(ApiResponse.success(updated, "İşletme bilgileri başarıyla güncellendi"));
    }
    
    // ========== SERVICE MANAGEMENT ENDPOINTS ==========
    
    @GetMapping("/{businessId}/services")
    public ResponseEntity<ApiResponse<List<ServiceResponse>>> getServicesByBusinessId(@PathVariable Long businessId) {
        List<ServiceResponse> services = serviceService.getServicesByBusinessId(businessId);
        return ResponseEntity.ok(ApiResponse.success(services, "Hizmetler başarıyla getirildi"));
    }
    
    @PostMapping("/{businessId}/services")
    @PreAuthorize("hasRole('BUSINESS_OWNER')")
    public ResponseEntity<ApiResponse<ServiceResponse>> createService(
            @PathVariable Long businessId,
            @Valid @RequestBody ServiceRequest request,
            HttpServletRequest req) {
        log.debug("Creating service for businessId: {}, request: {}", businessId, request);
        String token = jwtService.resolveToken(req);
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Token bulunamadı"));
        }
        Long ownerId = jwtService.getUserIdFromToken(token);
        if (ownerId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Token'dan kullanıcı ID'si alınamadı"));
        }
        // Business ownership is validated in service layer
        ServiceResponse service = serviceService.createService(businessId, request, ownerId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(service, "Hizmet başarıyla eklendi"));
    }
    
    @PutMapping("/{businessId}/services/{serviceId}")
    @PreAuthorize("hasRole('BUSINESS_OWNER')")
    public ResponseEntity<ApiResponse<ServiceResponse>> updateService(
            @PathVariable Long businessId,
            @PathVariable Long serviceId,
            @Valid @RequestBody ServiceRequest request,
            HttpServletRequest req) {
        Long ownerId = jwtService.getUserIdFromToken(jwtService.resolveToken(req));
        // Business ownership is validated in service layer
        ServiceResponse service = serviceService.updateService(serviceId, request, ownerId);
        // Verify service belongs to the businessId
        if (!service.getBusinessId().equals(businessId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Bu hizmet bu işletmeye ait değil"));
        }
        return ResponseEntity.ok(ApiResponse.success(service, "Hizmet başarıyla güncellendi"));
    }
    
    @DeleteMapping("/{businessId}/services/{serviceId}")
    @PreAuthorize("hasRole('BUSINESS_OWNER')")
    public ResponseEntity<ApiResponse<Void>> deleteService(
            @PathVariable Long businessId,
            @PathVariable Long serviceId,
            HttpServletRequest req) {
        Long ownerId = jwtService.getUserIdFromToken(jwtService.resolveToken(req));
        // Verify service belongs to the businessId before deletion
        ServiceResponse service = serviceService.getServiceById(serviceId);
        if (!service.getBusinessId().equals(businessId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Bu hizmet bu işletmeye ait değil"));
        }
        serviceService.deleteService(serviceId, ownerId);
        return ResponseEntity.ok(ApiResponse.success(null, "Hizmet başarıyla silindi"));
    }
    
    // ========== EMPLOYEE MANAGEMENT ENDPOINTS ==========
    
    @GetMapping("/{businessId}/employees")
    public ResponseEntity<ApiResponse<List<EmployeeResponse>>> getEmployeesByBusinessId(@PathVariable Long businessId) {
        List<EmployeeResponse> employees = employeeService.getEmployeesByBusinessId(businessId);
        return ResponseEntity.ok(ApiResponse.success(employees, "Çalışanlar başarıyla getirildi"));
    }
    
    @PostMapping("/{businessId}/employees")
    @PreAuthorize("hasRole('BUSINESS_OWNER')")
    public ResponseEntity<ApiResponse<Object>> inviteEmployee(
            @PathVariable Long businessId,
            @RequestBody EmployeeRequest request,
            HttpServletRequest req) {
        String token = jwtService.resolveToken(req);
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Token bulunamadı"));
        }
        Long ownerId = jwtService.getUserIdFromToken(token);
        if (ownerId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Token'dan kullanıcı ID'si alınamadı"));
        }
        
        // Send invitation instead of directly creating employee
        // Only email is required for invitation
        if (request == null || request.getEmail() == null || request.getEmail().isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Email adresi gereklidir"));
        }
        
        // Validate email format
        if (!request.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Geçersiz email formatı"));
        }
        
        try {
            StaffInvitation invitation = staffInvitationService.sendInvitation(businessId, request.getEmail(), ownerId);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(invitation, "Çalışan davetiyesi gönderildi. Kullanıcı davetiyeyi kabul ettiğinde çalışan olarak eklenecektir."));
        } catch (BusinessException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @PutMapping("/{businessId}/employees/{employeeId}")
    @PreAuthorize("hasRole('BUSINESS_OWNER')")
    public ResponseEntity<ApiResponse<EmployeeResponse>> updateEmployee(
            @PathVariable Long businessId,
            @PathVariable Long employeeId,
            @Valid @RequestBody EmployeeRequest request,
            HttpServletRequest req) {
        Long ownerId = jwtService.getUserIdFromToken(jwtService.resolveToken(req));
        // Business ownership is validated in service layer
        EmployeeResponse employee = employeeService.updateEmployee(employeeId, request, ownerId);
        // Verify employee belongs to the businessId
        if (!employee.getBusinessId().equals(businessId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Bu çalışan bu işletmeye ait değil"));
        }
        return ResponseEntity.ok(ApiResponse.success(employee, "Çalışan başarıyla güncellendi"));
    }
    
    @DeleteMapping("/{businessId}/employees/{employeeId}")
    @PreAuthorize("hasRole('BUSINESS_OWNER')")
    public ResponseEntity<ApiResponse<Void>> deleteEmployee(
            @PathVariable Long businessId,
            @PathVariable Long employeeId,
            HttpServletRequest req) {
        Long ownerId = jwtService.getUserIdFromToken(jwtService.resolveToken(req));
        // Verify employee belongs to the businessId before deletion
        EmployeeResponse employee = employeeService.getEmployeeById(employeeId);
        if (!employee.getBusinessId().equals(businessId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Bu çalışan bu işletmeye ait değil"));
        }
        employeeService.deleteEmployee(employeeId, ownerId);
        return ResponseEntity.ok(ApiResponse.success(null, "Çalışan başarıyla silindi"));
    }
    
    // ========== WORK SCHEDULE ENDPOINTS ==========
    
    @GetMapping("/{businessId}/employees/{employeeId}/schedule")
    public ResponseEntity<ApiResponse<List<WorkScheduleResponse>>> getEmployeeSchedule(
            @PathVariable Long businessId,
            @PathVariable Long employeeId) {
        // Verify employee belongs to the businessId
        EmployeeResponse employee = employeeService.getEmployeeById(employeeId);
        if (!employee.getBusinessId().equals(businessId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Bu çalışan bu işletmeye ait değil"));
        }
        List<WorkScheduleResponse> schedules = workScheduleService.getSchedulesByEmployeeId(employeeId);
        return ResponseEntity.ok(ApiResponse.success(schedules, "Çalışma saatleri başarıyla getirildi"));
    }
    
    @PostMapping("/{businessId}/employees/{employeeId}/schedule")
    @PreAuthorize("hasRole('BUSINESS_OWNER')")
    public ResponseEntity<ApiResponse<List<WorkScheduleResponse>>> updateEmployeeSchedule(
            @PathVariable Long businessId,
            @PathVariable Long employeeId,
            @Valid @RequestBody BatchWorkScheduleRequest request,
            HttpServletRequest req) {
        Long ownerId = jwtService.getUserIdFromToken(jwtService.resolveToken(req));
        // Verify employee belongs to the businessId
        EmployeeResponse employee = employeeService.getEmployeeById(employeeId);
        if (!employee.getBusinessId().equals(businessId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Bu çalışan bu işletmeye ait değil"));
        }
        // Business ownership is validated in service layer
        List<WorkScheduleResponse> schedules = workScheduleService.updateEmployeeSchedules(employeeId, request, ownerId);
        return ResponseEntity.ok(ApiResponse.success(schedules, "Çalışma saatleri başarıyla güncellendi"));
    }
}
