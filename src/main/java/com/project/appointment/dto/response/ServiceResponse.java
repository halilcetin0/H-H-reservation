package com.project.appointment.dto.response;

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
public class ServiceResponse {
    private Long id;
    private Long businessId;
    private String name;
    private String description;
    private Integer durationMinutes;
    private BigDecimal price;
    private Boolean isActive;
    private LocalDateTime createdAt;
}

