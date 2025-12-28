package com.project.appointment.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceRequest {
    
    @NotBlank(message = "Service name is required")
    @Size(min = 2, max = 255, message = "Name must be between 2 and 255 characters")
    private String name;
    
    @Size(max = 2000)
    private String description;
    
    // Support both 'duration' and 'durationMinutes' for frontend compatibility
    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be a positive number (minutes)")
    @Max(value = 1440, message = "Duration must not exceed 1440 minutes")
    @JsonAlias({"duration", "durationMinutes"})
    private Integer durationMinutes;
    
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", inclusive = true, message = "Price must be a positive number")
    private BigDecimal price;
}

