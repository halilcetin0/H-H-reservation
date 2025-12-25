package com.project.appointment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardResponse {
    private Long totalAppointments;
    private Long todayAppointments;
    private Long upcomingAppointments;
    private Long completedAppointments;
    private Long cancelledAppointments;
    private Double totalRevenue;
    private Double monthlyRevenue;
    private Double averageRating;
    private Map<String, Long> appointmentsByStatus;
    private Map<String, Long> last7DaysAppointments;
    private List<Map<String, Object>> topServices;
    private List<Map<String, Object>> topEmployees;
}
