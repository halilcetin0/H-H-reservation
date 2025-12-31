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
    public void acceptInvitation(String token, Long userId) {
        try {
            log.debug("Accepting invitation with token: {} for user: {}", token, userId);
            
            StaffInvitation invitation = invitationRepository.findByToken(token)
                    .orElseThrow(() -> {
                        log.warn("Invitation not found for token: {}", token);
                        return new ResourceNotFoundException("Invitation not found");
                    });
            
            log.debug("Found invitation: {} for business: {}", invitation.getId(), invitation.getBusiness().getId());
            
            // Verify invitation is still valid
            if (invitation.getStatus() != InvitationStatus.PENDING) {
                log.warn("Invitation {} is no longer valid. Status: {}", invitation.getId(), invitation.getStatus());
                throw new BusinessException("Invitation is no longer valid");
            }
            
            if (invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
                log.warn("Invitation {} has expired", invitation.getId());
                invitation.setStatus(InvitationStatus.EXPIRED);
                invitationRepository.save(invitation);
                throw new BusinessException("Invitation has expired");
            }
            
            // Get user account
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> {
                        log.warn("User not found: {}", userId);
                        return new ResourceNotFoundException("User not found");
                    });
            
            log.debug("Found user: {} with email: {}", user.getId(), user.getEmail());
            
            // Verify email matches
            if (!user.getEmail().equalsIgnoreCase(invitation.getEmail())) {
                log.warn("Email mismatch: user email {} does not match invitation email {}", user.getEmail(), invitation.getEmail());
                throw new BusinessException("Invitation email does not match your account email");
            }
            
            // Get business to ensure it's loaded
            Business business = invitation.getBusiness();
            if (business == null) {
                log.error("Business is null for invitation: {}", invitation.getId());
                throw new BusinessException("Business not found for this invitation");
            }
            
            Long businessId = business.getId();
            log.debug("Checking if user {} is already an employee for business {}", userId, businessId);
            
            // Check if user is already an employee for this business
            employeeRepository.findByBusinessIdAndUserId(businessId, userId)
                    .ifPresent(emp -> {
                        log.warn("User {} is already an employee for business {}", userId, businessId);
                        throw new BusinessException("You are already an employee for this business");
                    });
            
            // Create employee and link with user account
            String employeeName = user.getFullName();
            if (employeeName == null || employeeName.trim().isEmpty()) {
                employeeName = invitation.getEmail();
                log.debug("Using email as employee name since fullName is null or empty");
            }
            
            Employee employee = Employee.builder()
                    .business(business)
                    .user(user)
                    .email(invitation.getEmail())
                    .name(employeeName)
                    .phone(user.getPhone()) // Use user's phone if available (can be null)
                    .isActive(true)
                    .build();
            
            log.debug("Creating employee for business {} with name: {}", businessId, employeeName);
            employee = employeeRepository.save(employee);
            log.debug("Employee created with ID: {}", employee.getId());
            
            // Update user role to STAFF if not already
            if (user.getRole() != Role.STAFF && user.getRole() != Role.BUSINESS_OWNER && user.getRole() != Role.ADMIN) {
                log.debug("Updating user {} role to STAFF", userId);
                user.setRole(Role.STAFF);
                userRepository.save(user);
            }
            
            // Update invitation status
            invitation.setStatus(InvitationStatus.ACCEPTED);
            invitationRepository.save(invitation);
            
            log.info("Staff invitation accepted by user {} for business {}", userId, businessId);
        } catch (BusinessException | ResourceNotFoundException e) {
            // Re-throw known exceptions
            throw e;
        } catch (Exception e) {
            log.error("Error accepting invitation with token: {} for user: {}", token, userId, e);
            throw new BusinessException("Failed to accept invitation: " + e.getMessage());
        }
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

