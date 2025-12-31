package com.project.appointment.controller;

import com.project.appointment.dto.request.ReviewRequest;
import com.project.appointment.dto.response.ApiResponse;
import com.project.appointment.dto.response.ReviewResponse;
import com.project.appointment.security.JwtService;
import com.project.appointment.service.ReviewService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {
    
    private final ReviewService reviewService;
    private final JwtService jwtService;
    
    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(
            @Valid @RequestBody ReviewRequest request,
            HttpServletRequest req) {
        if (request.getAppointmentId() == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Appointment ID is required"));
        }
        Long userId = jwtService.getUserIdFromToken(jwtService.resolveToken(req));
        ReviewResponse review = reviewService.createReview(request.getAppointmentId(), request, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(review, "Yorum başarıyla eklendi"));
    }
    
    @PostMapping("/appointments/{appointmentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReviewResponse> createReviewByAppointmentId(
            @PathVariable Long appointmentId,
            @Valid @RequestBody ReviewRequest request,
            HttpServletRequest req) {
        Long userId = jwtService.getUserIdFromToken(jwtService.resolveToken(req));
        return ResponseEntity.status(HttpStatus.CREATED).body(reviewService.createReview(appointmentId, request, userId));
    }
    
    @GetMapping("/me")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getMyReviews(HttpServletRequest req) {
        Long userId = jwtService.getUserIdFromToken(jwtService.resolveToken(req));
        List<ReviewResponse> reviews = reviewService.getUserReviews(userId);
        return ResponseEntity.ok(ApiResponse.success(reviews, "Yorumlar başarıyla getirildi"));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<ReviewResponse>> updateReview(
            @PathVariable Long id,
            @Valid @RequestBody ReviewRequest request,
            HttpServletRequest req) {
        Long userId = jwtService.getUserIdFromToken(jwtService.resolveToken(req));
        ReviewResponse review = reviewService.updateReview(id, request, userId);
        return ResponseEntity.ok(ApiResponse.success(review, "Yorum başarıyla güncellendi"));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<Void>> deleteReview(
            @PathVariable Long id,
            HttpServletRequest req) {
        Long userId = jwtService.getUserIdFromToken(jwtService.resolveToken(req));
        reviewService.deleteReview(id, userId);
        return ResponseEntity.ok(ApiResponse.success(null, "Yorum başarıyla silindi"));
    }
    
    @GetMapping("/appointments/{appointmentId}")
    public ResponseEntity<ReviewResponse> getReviewByAppointmentId(@PathVariable Long appointmentId) {
        return ResponseEntity.ok(reviewService.getReviewByAppointmentId(appointmentId));
    }
}
