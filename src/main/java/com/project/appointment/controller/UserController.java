package com.project.appointment.controller;

import com.project.appointment.dto.request.UpdatePasswordRequest;
import com.project.appointment.dto.request.UpdateUserRequest;
import com.project.appointment.dto.response.ApiResponse;
import com.project.appointment.dto.response.UserResponse;
import com.project.appointment.security.JwtService;
import com.project.appointment.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    private final JwtService jwtService;
    
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(HttpServletRequest req) {
        Long userId = jwtService.getUserIdFromToken(jwtService.resolveToken(req));
        UserResponse user = userService.getCurrentUser(userId);
        return ResponseEntity.ok(ApiResponse.success(user, "Kullanıcı bilgileri başarıyla getirildi"));
    }
    
    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserProfile(
            @Valid @RequestBody UpdateUserRequest request,
            HttpServletRequest req) {
        Long userId = jwtService.getUserIdFromToken(jwtService.resolveToken(req));
        UserResponse user = userService.updateUserProfile(userId, request);
        return ResponseEntity.ok(ApiResponse.success(user, "Profil bilgileri başarıyla güncellendi"));
    }
    
    @PutMapping("/me/password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody UpdatePasswordRequest request,
            HttpServletRequest req) {
        Long userId = jwtService.getUserIdFromToken(jwtService.resolveToken(req));
        userService.changePassword(userId, request);
        return ResponseEntity.ok(ApiResponse.success(null, "Şifre başarıyla değiştirildi"));
    }
}

