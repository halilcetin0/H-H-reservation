package com.project.appointment.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "businesses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Business {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "owner_id", unique = true, nullable = false)
    private Long ownerId;
    
    @Column(nullable = false)
    private String name;
    
    @Column(length = 5000)
    private String description;
    
    private String category;
    
    @Column(length = 500)
    private String address;
    
    private String city;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "business_type")
    private BusinessType businessType;
    
    private String phone;
    
    private String email;
    
    @Column(name = "image_url", length = 500)
    private String imageUrl;
    
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "business", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Service> services = new ArrayList<>();
    
    @OneToMany(mappedBy = "business", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Employee> employees = new ArrayList<>();
    
    @OneToMany(mappedBy = "business", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Appointment> appointments = new ArrayList<>();
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

