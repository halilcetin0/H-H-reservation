package com.project.appointment.service;

import com.project.appointment.dto.request.LoginRequest;
import com.project.appointment.dto.request.RegisterRequest;
import com.project.appointment.dto.response.AuthResponse;
import com.project.appointment.entity.Role;
import com.project.appointment.entity.User;
import com.project.appointment.repository.UserRepository;
import com.project.appointment.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final EmailService emailService;
    
    @Value("${app.url}")
    private String appUrl;
    
    @Value("${app.email.verification-token-expiration}")
    private long verificationTokenExpiration;
    
    @Value("${app.email.password-reset-token-expiration}")
    private long passwordResetTokenExpiration;
    
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        
        // Generate verification token
        String verificationToken = UUID.randomUUID().toString();
        LocalDateTime tokenExpiration = LocalDateTime.now().plusSeconds(verificationTokenExpiration / 1000);
        
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .role(request.getRole() != null ? request.getRole() : Role.CUSTOMER)
                .emailVerified(false)
                .verificationToken(verificationToken)
                .verificationTokenExpiresAt(tokenExpiration)
                .build();
        
        userRepository.save(user);
        
        // Send verification email
        String verificationLink = appUrl + "/api/auth/verify-email?token=" + verificationToken;
        emailService.sendVerificationEmail(user.getEmail(), user.getFullName(), verificationLink);
        
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);
        
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .build();
    }
    
    public AuthResponse login(LoginRequest request) {
        // Check if user exists and email is verified
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));
        
        if (!user.isEmailVerified()) {
            throw new DisabledException("Email not verified. Please check your email for verification link.");
        }
        
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);
        
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .build();
    }
    
    @Transactional
    public void verifyEmail(String token) {
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid verification token"));
        
        if (user.getVerificationTokenExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Verification token has expired");
        }
        
        user.setEmailVerified(true);
        user.setVerificationToken(null);
        user.setVerificationTokenExpiresAt(null);
        userRepository.save(user);
    }
    
    @Transactional
    public void resendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (user.isEmailVerified()) {
            throw new RuntimeException("Email already verified");
        }
        
        // Generate new verification token
        String verificationToken = UUID.randomUUID().toString();
        LocalDateTime tokenExpiration = LocalDateTime.now().plusSeconds(verificationTokenExpiration / 1000);
        
        user.setVerificationToken(verificationToken);
        user.setVerificationTokenExpiresAt(tokenExpiration);
        userRepository.save(user);
        
        // Send verification email
        String verificationLink = appUrl + "/api/auth/verify-email?token=" + verificationToken;
        emailService.sendVerificationEmail(user.getEmail(), user.getFullName(), verificationLink);
    }
    
    @Transactional
    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Generate password reset token
        String resetToken = UUID.randomUUID().toString();
        LocalDateTime tokenExpiration = LocalDateTime.now().plusSeconds(passwordResetTokenExpiration / 1000);
        
        user.setPasswordResetToken(resetToken);
        user.setPasswordResetTokenExpiresAt(tokenExpiration);
        userRepository.save(user);
        
        // Send password reset email
        String resetLink = appUrl + "/api/auth/reset-password?token=" + resetToken;
        emailService.sendPasswordResetEmail(user.getEmail(), user.getFullName(), resetLink);
    }
    
    @Transactional
    public void resetPassword(String token, String newPassword) {
        User user = userRepository.findByPasswordResetToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid password reset token"));
        
        if (user.getPasswordResetTokenExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Password reset token has expired");
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordResetToken(null);
        user.setPasswordResetTokenExpiresAt(null);
        userRepository.save(user);
    }
}


