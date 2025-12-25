package com.project.appointment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewResponse {
    private Long id;
    private Long appointmentId;
    private Long customerId;
    private String customerName;
    private Long businessId;
    private Long employeeId;
    private String employeeName;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
}

