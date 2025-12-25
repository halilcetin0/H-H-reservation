package com.project.appointment.dto.response;

import com.project.appointment.entity.BusinessType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusinessResponse {
    private Long id;
    private Long ownerId;
    private String name;
    private String description;
    private String category;
    private String address;
    private String city;
    private BusinessType businessType;
    private String phone;
    private String email;
    private String imageUrl;
    private Boolean isActive;
    private Double averageRating;
    private Long favoriteCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

