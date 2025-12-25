package com.project.appointment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeAnalyticsResponse {
    private Long employeeId;
    private String employeeName;
    private Long totalAppointments;
    private Double totalEarnings;
    private Double averageRating;
}

