package com.project.appointment.controller;

import com.project.appointment.dto.request.ReviewRequest;
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

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {
    
    private final ReviewService reviewService;
    private final JwtService jwtService;
    
    @PostMapping("/appointments/{appointmentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReviewResponse> createReview(
            @PathVariable Long appointmentId,
            @Valid @RequestBody ReviewRequest request,
            HttpServletRequest req) {
        Long userId = jwtService.getUserIdFromToken(jwtService.resolveToken(req));
        return ResponseEntity.status(HttpStatus.CREATED).body(reviewService.createReview(appointmentId, request, userId));
    }
    
    @GetMapping("/appointments/{appointmentId}")
    public ResponseEntity<ReviewResponse> getReviewByAppointmentId(@PathVariable Long appointmentId) {
        return ResponseEntity.ok(reviewService.getReviewByAppointmentId(appointmentId));
    }
}
