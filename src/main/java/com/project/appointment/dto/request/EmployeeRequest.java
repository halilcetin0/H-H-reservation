package com.project.appointment.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeRequest {
    
    @NotBlank(message = "Employee name is required")
    @Size(min = 2, max = 255, message = "Name must be between 2 and 255 characters")
    private String name;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
    
    // Phone is optional - validate format only if provided
    @Pattern(regexp = "^$|^[0-9+\\s()-]{10,20}$", message = "Invalid phone number format (10-20 digits)")
    private String phone; // Optional
    
    private String title;
    
    private String specialization;
    
    private String imageUrl;
}

