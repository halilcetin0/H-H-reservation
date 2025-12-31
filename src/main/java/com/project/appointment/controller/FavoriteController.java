package com.project.appointment.controller;

import com.project.appointment.dto.response.ApiResponse;
import com.project.appointment.dto.response.BusinessResponse;
import com.project.appointment.security.JwtService;
import com.project.appointment.service.FavoriteService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {
    
    private final FavoriteService favoriteService;
    private final JwtService jwtService;
    
    @PostMapping("/{businessId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<Void>> addFavorite(@PathVariable Long businessId, HttpServletRequest req) {
        Long userId = jwtService.getUserIdFromToken(jwtService.resolveToken(req));
        try {
            favoriteService.addFavorite(businessId, userId);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(null, "İşletme favorilere eklendi"));
        } catch (com.project.appointment.exception.BusinessException e) {
            // If already favorited, return success (idempotent)
            if (e.getMessage().contains("already in favorites")) {
                return ResponseEntity.ok(ApiResponse.success(null, "İşletme zaten favorilerinizde"));
            }
            throw e;
        }
    }
    
    @DeleteMapping("/{businessId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<Void>> removeFavorite(@PathVariable Long businessId, HttpServletRequest req) {
        Long userId = jwtService.getUserIdFromToken(jwtService.resolveToken(req));
        favoriteService.removeFavorite(businessId, userId);
        return ResponseEntity.ok(ApiResponse.success(null, "İşletme favorilerden kaldırıldı"));
    }
    
    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<List<BusinessResponse>>> getFavorites(HttpServletRequest req) {
        Long userId = jwtService.getUserIdFromToken(jwtService.resolveToken(req));
        List<BusinessResponse> favorites = favoriteService.getUserFavorites(userId);
        return ResponseEntity.ok(ApiResponse.success(favorites, "Favoriler başarıyla getirildi"));
    }
    
    @GetMapping("/{businessId}/check")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<Boolean>> checkFavorite(@PathVariable Long businessId, HttpServletRequest req) {
        Long userId = jwtService.getUserIdFromToken(jwtService.resolveToken(req));
        boolean isFavorite = favoriteService.isFavorite(businessId, userId);
        return ResponseEntity.ok(ApiResponse.success(isFavorite, "Favori durumu kontrol edildi"));
    }
    
    @GetMapping("/count/{businessId}")
    public ResponseEntity<ApiResponse<Long>> getFavoriteCount(@PathVariable Long businessId) {
        Long count = favoriteService.getFavoriteCount(businessId);
        return ResponseEntity.ok(ApiResponse.success(count, "Favori sayısı başarıyla getirildi"));
    }
}
