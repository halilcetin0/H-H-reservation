package com.project.appointment.dto.request;

import com.project.appointment.entity.AppointmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentSearchRequest {
    
    private Long businessId;
    private Long customerId;
    private Long employeeId;
    private AppointmentStatus status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private int page = 0;
    private int size = 20;
}

