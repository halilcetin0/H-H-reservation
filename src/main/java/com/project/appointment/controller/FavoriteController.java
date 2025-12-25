package com.project.appointment.controller;

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
import java.util.Map;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {
    
    private final FavoriteService favoriteService;
    private final JwtService jwtService;
    
    @PostMapping("/{businessId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> addFavorite(@PathVariable Long businessId, HttpServletRequest req) {
        Long userId = jwtService.getUserIdFromToken(jwtService.resolveToken(req));
        favoriteService.addFavorite(businessId, userId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    
    @DeleteMapping("/{businessId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> removeFavorite(@PathVariable Long businessId, HttpServletRequest req) {
        Long userId = jwtService.getUserIdFromToken(jwtService.resolveToken(req));
        favoriteService.removeFavorite(businessId, userId);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<BusinessResponse>> getFavorites(HttpServletRequest req) {
        Long userId = jwtService.getUserIdFromToken(jwtService.resolveToken(req));
        return ResponseEntity.ok(favoriteService.getUserFavorites(userId));
    }
    
    @GetMapping("/check/{businessId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Boolean>> checkFavorite(@PathVariable Long businessId, HttpServletRequest req) {
        Long userId = jwtService.getUserIdFromToken(jwtService.resolveToken(req));
        return ResponseEntity.ok(Map.of("isFavorite", favoriteService.isFavorite(businessId, userId)));
    }
    
    @GetMapping("/count/{businessId}")
    public ResponseEntity<Map<String, Long>> getFavoriteCount(@PathVariable Long businessId) {
        return ResponseEntity.ok(Map.of("count", favoriteService.getFavoriteCount(businessId)));
    }
}
