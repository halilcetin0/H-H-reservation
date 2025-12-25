package com.project.appointment.controller;

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
import java.util.Map;

@RestController
@RequestMapping("/api/staff-invitations")
@RequiredArgsConstructor
public class StaffInvitationController {
    
    private final StaffInvitationService invitationService;
    private final JwtService jwtService;
    
    @PostMapping
    @PreAuthorize("hasRole('BUSINESS_OWNER')")
    public ResponseEntity<StaffInvitation> sendInvitation(
            @RequestParam Long businessId,
            @RequestParam String email,
            HttpServletRequest req) {
        Long ownerId = jwtService.getUserIdFromToken(jwtService.resolveToken(req));
        return ResponseEntity.status(HttpStatus.CREATED).body(invitationService.sendInvitation(businessId, email, ownerId));
    }
    
    @PostMapping("/accept")
    public ResponseEntity<Map<String, String>> acceptInvitation(@RequestParam String token) {
        invitationService.acceptInvitation(token);
        return ResponseEntity.ok(Map.of("message", "Invitation accepted successfully"));
    }
    
    @PostMapping("/reject")
    public ResponseEntity<Map<String, String>> rejectInvitation(@RequestParam String token) {
        invitationService.rejectInvitation(token);
        return ResponseEntity.ok(Map.of("message", "Invitation rejected"));
    }
    
    @GetMapping
    @PreAuthorize("hasRole('BUSINESS_OWNER')")
    public ResponseEntity<List<StaffInvitation>> getBusinessInvitations(
            @RequestParam Long businessId,
            HttpServletRequest req) {
        Long ownerId = jwtService.getUserIdFromToken(jwtService.resolveToken(req));
        return ResponseEntity.ok(invitationService.getBusinessInvitations(businessId, ownerId));
    }
}
