package com.project.appointment.service;

import com.project.appointment.dto.request.NotificationPreferenceRequest;
import com.project.appointment.entity.NotificationPreference;
import com.project.appointment.repository.NotificationPreferenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationPreferenceService {
    
    private final NotificationPreferenceRepository preferenceRepository;
    
    public NotificationPreference getUserPreferences(Long userId) {
        return preferenceRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultPreferences(userId));
    }
    
    @Transactional
    public NotificationPreference updatePreferences(Long userId, NotificationPreferenceRequest request) {
        NotificationPreference preference = preferenceRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultPreferences(userId));
        
        if (request.getEmailNotifications() != null) {
            preference.setEmailNotifications(request.getEmailNotifications());
        }
        if (request.getAppointmentReminders() != null) {
            preference.setAppointmentReminders(request.getAppointmentReminders());
        }
        if (request.getAppointmentConfirmations() != null) {
            preference.setAppointmentConfirmations(request.getAppointmentConfirmations());
        }
        if (request.getAppointmentCancellations() != null) {
            preference.setAppointmentCancellations(request.getAppointmentCancellations());
        }
        if (request.getMarketingEmails() != null) {
            preference.setMarketingEmails(request.getMarketingEmails());
        }
        
        preference = preferenceRepository.save(preference);
        log.info("Notification preferences updated for user: {}", userId);
        
        return preference;
    }
    
    private NotificationPreference createDefaultPreferences(Long userId) {
        NotificationPreference preference = NotificationPreference.builder()
                .userId(userId)
                .emailNotifications(true)
                .appointmentReminders(true)
                .appointmentConfirmations(true)
                .appointmentCancellations(true)
                .marketingEmails(false)
                .build();
        
        return preferenceRepository.save(preference);
    }
}
