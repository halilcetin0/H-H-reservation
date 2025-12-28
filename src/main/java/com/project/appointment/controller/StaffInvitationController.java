package com.project.appointment.controller;

import com.project.appointment.dto.response.ApiResponse;
import com.project.appointment.entity.StaffInvitation;
import com.project.appointment.security.JwtService;
import com.project.appointment.service.StaffInvitationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/staff-invitations")
@RequiredArgsConstructor
public class StaffInvitationController {
    
    private final StaffInvitationService invitationService;
    private final JwtService jwtService;
    
    @PostMapping
    @PreAuthorize("hasRole('BUSINESS_OWNER')")
    public ResponseEntity<ApiResponse<StaffInvitation>> sendInvitation(
            @RequestParam Long businessId,
            @RequestParam String email,
            HttpServletRequest req) {
        Long ownerId = jwtService.getUserIdFromToken(jwtService.resolveToken(req));
        StaffInvitation invitation = invitationService.sendInvitation(businessId, email, ownerId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(invitation, "Çalışan davetiyesi başarıyla gönderildi"));
    }
    
    @PostMapping("/accept")
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
    
    @PostMapping("/reject")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Object>> rejectInvitation(
            @RequestParam String token,
            HttpServletRequest req) {
        Long userId = jwtService.getUserIdFromToken(jwtService.resolveToken(req));
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Kullanıcı kimlik doğrulaması yapılmadı"));
        }
        invitationService.rejectInvitation(token);
        return ResponseEntity.ok(ApiResponse.success(null, "Davetiye reddedildi"));
    }
    
    @GetMapping
    @PreAuthorize("hasRole('BUSINESS_OWNER')")
    public ResponseEntity<ApiResponse<List<StaffInvitation>>> getBusinessInvitations(
            @RequestParam Long businessId,
            HttpServletRequest req) {
        Long ownerId = jwtService.getUserIdFromToken(jwtService.resolveToken(req));
        List<StaffInvitation> invitations = invitationService.getBusinessInvitations(businessId, ownerId);
        return ResponseEntity.ok(ApiResponse.success(invitations, "Davetiyeler başarıyla getirildi"));
    }
}
