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
    @Size(max = 255)
    private String name;
    
    @Email(message = "Invalid email format")
    private String email;
    
    @Pattern(regexp = "^[0-9]{10,15}$", message = "Invalid phone number")
    private String phone;
    
    private String title;
    
    private String imageUrl;
}

