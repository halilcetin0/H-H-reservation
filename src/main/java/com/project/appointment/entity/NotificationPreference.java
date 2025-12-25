package com.project.appointment.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification_preferences")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationPreference {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", unique = true, nullable = false)
    private Long userId;
    
    @Column(name = "email_notifications", nullable = false)
    @Builder.Default
    private Boolean emailNotifications = true;
    
    @Column(name = "appointment_reminders", nullable = false)
    @Builder.Default
    private Boolean appointmentReminders = true;
    
    @Column(name = "appointment_confirmations", nullable = false)
    @Builder.Default
    private Boolean appointmentConfirmations = true;
    
    @Column(name = "appointment_cancellations", nullable = false)
    @Builder.Default
    private Boolean appointmentCancellations = true;
    
    @Column(name = "marketing_emails", nullable = false)
    @Builder.Default
    private Boolean marketingEmails = false;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
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

