package com.project.appointment.service;

import com.project.appointment.dto.request.UpdatePasswordRequest;
import com.project.appointment.dto.request.UpdateUserRequest;
import com.project.appointment.dto.response.UserResponse;
import com.project.appointment.entity.User;
import com.project.appointment.exception.BusinessException;
import com.project.appointment.exception.ResourceNotFoundException;
import com.project.appointment.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    public UserResponse getCurrentUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return mapToResponse(user);
    }
    
    @Transactional
    public UserResponse updateUserProfile(Long userId, UpdateUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            user.setFullName(request.getFullName());
        }
        
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        
        user = userRepository.save(user);
        log.info("User profile updated: {}", userId);
        
        return mapToResponse(user);
    }
    
    @Transactional
    public void changePassword(Long userId, UpdatePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BusinessException("Current password is incorrect");
        }
        
        // Check if new password is different
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new BusinessException("New password must be different from current password");
        }
        
        // Validate new password length
        if (request.getNewPassword().length() < 6) {
            throw new BusinessException("New password must be at least 6 characters");
        }
        
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("Password changed for user: {}", userId);
    }
    
    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .role(user.getRole())
                .emailVerified(user.isEmailVerified())
                .build();
    }
}

