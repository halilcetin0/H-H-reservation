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
public class EmployeeResponse {
    private Long id;
    private Long businessId;
    private String name;
    private String email;
    private String phone;
    private String title;
    private String imageUrl;
    private Boolean isActive;
    private LocalDateTime createdAt;
}

