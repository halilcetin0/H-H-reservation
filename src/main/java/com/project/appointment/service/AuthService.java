package com.project.appointment.service;

import com.project.appointment.dto.request.LoginRequest;
import com.project.appointment.dto.request.RegisterRequest;
import com.project.appointment.dto.response.AuthResponse;
import com.project.appointment.entity.Role;
import com.project.appointment.entity.User;
import com.project.appointment.repository.UserRepository;
import com.project.appointment.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    
    @Value("${app.url}")
    private String appUrl;
    
    @Value("${app.frontend-url}")
    private String frontendUrl;
    
    @Value("${app.email.verification-token-expiration}")
    private long verificationTokenExpiration;
    
    @Value("${app.email.password-reset-token-expiration}")
    private long passwordResetTokenExpiration;
    
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        
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
        
        user = userRepository.save(user);
        
        String verificationLink = appUrl + "/api/auth/verify-email?token=" + verificationToken;
        emailService.sendVerificationEmail(user.getEmail(), user.getFullName(), verificationLink);
        
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .build();
    }
    
    public AuthResponse login(LoginRequest request) {
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
        
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .build();
    }
    
    @Transactional
    public void verifyEmail(String token) {
        String normalizedToken = normalizeVerificationToken(token);
        User user = userRepository.findByVerificationToken(normalizedToken)
                .orElseThrow(() -> new RuntimeException(
                        "Invalid verification token. If you already verified your email, this link won't work again. " +
                                "Otherwise request a new one: POST /api/auth/resend-verification?email=you@example.com"
                ));

        // Make it idempotent: clicking the link twice shouldn't look like a mysterious failure.
        if (user.isEmailVerified()) {
            log.info("verifyEmail called for already verified userId={}", user.getId());
            return;
        }

        if (user.getVerificationTokenExpiresAt() == null) {
            throw new RuntimeException(
                    "Verification token is missing/invalid. Please request a new one: " +
                            "POST /api/auth/resend-verification?email=you@example.com"
            );
        }
        
        if (user.getVerificationTokenExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException(
                    "Verification token has expired. Please request a new one: " +
                            "POST /api/auth/resend-verification?email=you@example.com"
            );
        }
        
        user.setEmailVerified(true);
        user.setVerificationToken(null);
        user.setVerificationTokenExpiresAt(null);
        userRepository.save(user);
    }

    /**
     * Users often paste the full verification URL into the token field (especially from Swagger),
     * or tokens may contain spaces/quotes. This normalizes to the raw UUID string.
     */
    private String normalizeVerificationToken(String token) {
        if (token == null) {
            throw new RuntimeException("Verification token is required");
        }
        String t = token.trim();
        if (t.isEmpty()) {
            throw new RuntimeException("Verification token is required");
        }

        // If user pasted full URL (or partial) into token parameter, extract ?token=...
        if (t.contains("token=") || t.startsWith("http://") || t.startsWith("https://")) {
            try {
                String extracted = UriComponentsBuilder.fromUriString(t)
                        .build()
                        .getQueryParams()
                        .getFirst("token");
                if (extracted != null && !extracted.isBlank()) {
                    t = extracted.trim();
                } else if (t.contains("token=")) {
                    // fallback for non-URL strings
                    t = t.substring(t.lastIndexOf("token=") + "token=".length());
                    int amp = t.indexOf('&');
                    if (amp >= 0) t = t.substring(0, amp);
                    t = t.trim();
                }
            } catch (Exception ignored) {
                // fallback below
            }
        }

        // remove wrapping quotes that can appear when copy/pasting
        if ((t.startsWith("\"") && t.endsWith("\"")) || (t.startsWith("'") && t.endsWith("'"))) {
            t = t.substring(1, t.length() - 1).trim();
        }

        // sanity: token should look like UUID, but don't hard-fail if you change format later
        if (t.length() > 255) {
            throw new RuntimeException("Verification token format is invalid");
        }
        return t;
    }
    
    @Transactional
    public void resendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (user.isEmailVerified()) {
            throw new RuntimeException("Email already verified");
        }
        
        String verificationToken = UUID.randomUUID().toString();
        LocalDateTime tokenExpiration = LocalDateTime.now().plusSeconds(verificationTokenExpiration / 1000);
        
        user.setVerificationToken(verificationToken);
        user.setVerificationTokenExpiresAt(tokenExpiration);
        userRepository.save(user);
        
        String verificationLink = appUrl + "/api/auth/verify-email?token=" + verificationToken;
        emailService.sendVerificationEmail(user.getEmail(), user.getFullName(), verificationLink);
    }
    
    @Transactional
    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        String resetToken = UUID.randomUUID().toString();
        LocalDateTime tokenExpiration = LocalDateTime.now().plusSeconds(passwordResetTokenExpiration / 1000);
        
        user.setPasswordResetToken(resetToken);
        user.setPasswordResetTokenExpiresAt(tokenExpiration);
        userRepository.save(user);
        
        // Frontend URL'ine yönlendir
        String resetLink = frontendUrl + "/reset-password?token=" + resetToken;
        emailService.sendPasswordResetEmail(user.getEmail(), user.getFullName(), resetLink);
    }
    
    public boolean validateResetToken(String token) {
        try {
            User user = userRepository.findByPasswordResetToken(token)
                    .orElse(null);
            
            if (user == null) {
                return false;
            }
            
            if (user.getPasswordResetTokenExpiresAt() == null || 
                user.getPasswordResetTokenExpiresAt().isBefore(LocalDateTime.now())) {
                return false;
            }
            
            return true;
        } catch (Exception e) {
            log.error("Error validating reset token", e);
            return false;
        }
    }
    
    @Transactional
    public void resetPassword(String token, String newPassword) {
        User user = userRepository.findByPasswordResetToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid password reset token"));
        
        if (user.getPasswordResetTokenExpiresAt() == null) {
            throw new RuntimeException("Password reset token is invalid");
        }
        
        if (user.getPasswordResetTokenExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Password reset token has expired");
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordResetToken(null);
        user.setPasswordResetTokenExpiresAt(null);
        userRepository.save(user);
    }
}
