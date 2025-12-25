package com.project.appointment.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationPreferenceRequest {
    
    private Boolean emailNotifications;
    private Boolean appointmentReminders;
    private Boolean appointmentConfirmations;
    private Boolean appointmentCancellations;
    private Boolean marketingEmails;
}

