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
public class BusinessRequest {
    
    @NotBlank(message = "Business name is required")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;
    
    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;
    
    private String category;
    
    @Size(max = 500, message = "Address must not exceed 500 characters")
    private String address;
    
    private String city;
    
    private String businessType;
    
    @Pattern(regexp = "^[0-9]{10,15}$", message = "Invalid phone number")
    private String phone;
    
    @Email(message = "Invalid email format")
    private String email;
    
    private String imageUrl;
}

