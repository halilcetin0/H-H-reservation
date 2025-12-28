package com.project.appointment.dto.response;

import com.project.appointment.entity.AppointmentStatus;
import com.project.appointment.entity.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentResponse {
    private Long id;
    private Long customerId;
    private String customerName;
    private Long businessId;
    private String businessName;
    private Long serviceId;
    private String serviceName;
    private Long employeeId;
    private String employeeName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal price;
    private PaymentStatus paymentStatus;
    private AppointmentStatus status;
    private Boolean ownerApproved;
    private Boolean employeeApproved;
    private String notes;
    private String cancellationReason;
    private ReviewResponse review;
    private LocalDateTime createdAt;
}

