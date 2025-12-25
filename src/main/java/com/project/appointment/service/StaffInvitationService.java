package com.project.appointment.service;

import com.project.appointment.entity.*;
import com.project.appointment.exception.BusinessException;
import com.project.appointment.exception.ResourceNotFoundException;
import com.project.appointment.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class StaffInvitationService {
    
    private final StaffInvitationRepository invitationRepository;
    private final BusinessRepository businessRepository;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    
    @Transactional
    public StaffInvitation sendInvitation(Long businessId, String email, Long ownerId) {
        // Verify business exists and belongs to owner
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new BusinessException("Business not found"));
        
        if (!business.getOwnerId().equals(ownerId)) {
            throw new BusinessException("You don't have permission to invite staff to this business");
        }
        
        // Check if invitation already exists
        invitationRepository.findByBusinessIdAndEmailAndStatus(businessId, email, InvitationStatus.PENDING)
                .ifPresent(inv -> {
                    throw new BusinessException("Pending invitation already exists for this email");
                });
        
        // Generate token
        String token = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(7); // 7 days expiry
        
        StaffInvitation invitation = StaffInvitation.builder()
                .business(business)
                .email(email)
                .token(token)
                .status(InvitationStatus.PENDING)
                .expiresAt(expiresAt)
                .build();
        
        invitation = invitationRepository.save(invitation);
        
        // Send invitation email
        emailService.sendStaffInvitationEmail(email, business.getName(), token);
        
        log.info("Staff invitation sent to {} for business {}", email, businessId);
        return invitation;
    }
    
    @Transactional
    public void acceptInvitation(String token) {
        StaffInvitation invitation = invitationRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation not found"));
        
        // Verify invitation is still valid
        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new BusinessException("Invitation is no longer valid");
        }
        
        if (invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            invitation.setStatus(InvitationStatus.EXPIRED);
            invitationRepository.save(invitation);
            throw new BusinessException("Invitation has expired");
        }
        
        // Create employee
        Employee employee = Employee.builder()
                .business(invitation.getBusiness())
                .email(invitation.getEmail())
                .name("") // To be updated by employee
                .phone("") // To be updated by employee
                .build();
        
        employeeRepository.save(employee);
        
        // Update invitation status
        invitation.setStatus(InvitationStatus.ACCEPTED);
        invitationRepository.save(invitation);
        
        log.info("Staff invitation accepted: {}", token);
    }
    
    @Transactional
    public void rejectInvitation(String token) {
        StaffInvitation invitation = invitationRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation not found"));
        
        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new BusinessException("Invitation is no longer valid");
        }
        
        invitation.setStatus(InvitationStatus.REJECTED);
        invitationRepository.save(invitation);
        
        log.info("Staff invitation rejected: {}", token);
    }
    
    public List<StaffInvitation> getBusinessInvitations(Long businessId, Long ownerId) {
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new BusinessException("Business not found"));
        
        if (!business.getOwnerId().equals(ownerId)) {
            throw new BusinessException("You don't have permission to view invitations for this business");
        }
        
        return invitationRepository.findByBusinessId(businessId);
    }
}

