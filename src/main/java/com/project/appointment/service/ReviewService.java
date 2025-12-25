package com.project.appointment.service;

import com.project.appointment.dto.request.ReviewRequest;
import com.project.appointment.dto.response.ReviewResponse;
import com.project.appointment.entity.Appointment;
import com.project.appointment.entity.AppointmentStatus;
import com.project.appointment.entity.Review;
import com.project.appointment.exception.BusinessException;
import com.project.appointment.exception.ResourceNotFoundException;
import com.project.appointment.repository.AppointmentRepository;
import com.project.appointment.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {
    
    private final ReviewRepository reviewRepository;
    private final AppointmentRepository appointmentRepository;
    
    @Transactional
    public ReviewResponse createReview(Long appointmentId, ReviewRequest request, Long userId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));
        
        if (!appointment.getCustomer().getId().equals(userId)) {
            throw new BusinessException("You can only review your own appointments");
        }
        
        if (appointment.getStatus() != AppointmentStatus.COMPLETED) {
            throw new BusinessException("You can only review completed appointments");
        }
        
        if (reviewRepository.existsByAppointmentId(appointmentId)) {
            throw new BusinessException("You have already reviewed this appointment");
        }
        
        Review review = Review.builder()
                .appointment(appointment)
                .business(appointment.getBusiness())
                .employee(appointment.getEmployee())
                .customer(appointment.getCustomer())
                .rating(request.getRating())
                .comment(request.getComment())
                .build();
        
        review = reviewRepository.save(review);
        log.info("Review created: {} for appointment: {}", review.getId(), appointmentId);
        
        return mapToResponse(review);
    }
    
    public ReviewResponse getReviewByAppointmentId(Long appointmentId) {
        Review review = reviewRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));
        return mapToResponse(review);
    }
    
    private ReviewResponse mapToResponse(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .appointmentId(review.getAppointment().getId())
                .customerId(review.getCustomer().getId())
                .customerName(review.getCustomer().getFullName())
                .businessId(review.getBusiness().getId())
                .employeeId(review.getEmployee() != null ? review.getEmployee().getId() : null)
                .employeeName(review.getEmployee() != null ? review.getEmployee().getName() : null)
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
