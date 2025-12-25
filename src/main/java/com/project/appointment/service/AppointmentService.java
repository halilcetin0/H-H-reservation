package com.project.appointment.service;

import com.project.appointment.entity.Appointment;
import com.project.appointment.entity.AppointmentStatus;
import com.project.appointment.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentService {
    
    private final AppointmentRepository appointmentRepository;
    private final EmailService emailService;
    
    @Transactional
    public Appointment createAppointment(Appointment appointment) {
        appointment.setStatus(AppointmentStatus.PENDING);
        Appointment saved = appointmentRepository.save(appointment);
        
        // Send confirmation email
        String appointmentDetails = formatAppointmentDetails(saved);
        emailService.sendAppointmentConfirmationEmail(
            saved.getUser().getEmail(),
            saved.getUser().getFullName(),
            appointmentDetails
        );
        
        return saved;
    }
    
    @Transactional
    public Appointment confirmAppointment(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
        
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        return appointmentRepository.save(appointment);
    }
    
    @Transactional
    public Appointment cancelAppointment(Long appointmentId, String reason) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
        
        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.setCancellationReason(reason);
        Appointment cancelled = appointmentRepository.save(appointment);
        
        // Send cancellation email
        String appointmentDetails = formatAppointmentDetails(cancelled);
        emailService.sendAppointmentCancellationEmail(
            cancelled.getUser().getEmail(),
            cancelled.getUser().getFullName(),
            appointmentDetails,
            reason
        );
        
        return cancelled;
    }
    
    @Transactional
    public Appointment completeAppointment(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
        
        appointment.setStatus(AppointmentStatus.COMPLETED);
        return appointmentRepository.save(appointment);
    }
    
    public List<Appointment> getUserAppointments(Long userId) {
        return appointmentRepository.findByUserId(userId);
    }
    
    public List<Appointment> getServiceProviderAppointments(Long serviceProviderId) {
        return appointmentRepository.findByServiceProviderId(serviceProviderId);
    }
    
    // Scheduled task to send reminders (runs every hour)
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void sendAppointmentReminders() {
        log.info("Running appointment reminder task");
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime twentyFourHoursLater = now.plusHours(24);
        
        // Find appointments in the next 24 hours that haven't received reminders
        List<Appointment> upcomingAppointments = appointmentRepository
                .findByReminderSentFalseAndAppointmentDateBetween(now, twentyFourHoursLater);
        
        for (Appointment appointment : upcomingAppointments) {
            if (appointment.getStatus() == AppointmentStatus.CONFIRMED) {
                try {
                    String appointmentDetails = formatAppointmentDetails(appointment);
                    emailService.sendAppointmentReminderEmail(
                        appointment.getUser().getEmail(),
                        appointment.getUser().getFullName(),
                        appointmentDetails
                    );
                    
                    appointment.setReminderSent(true);
                    appointmentRepository.save(appointment);
                    
                    log.info("Reminder sent for appointment ID: {}", appointment.getId());
                } catch (Exception e) {
                    log.error("Failed to send reminder for appointment ID: {}", appointment.getId(), e);
                }
            }
        }
        
        log.info("Appointment reminder task completed. Sent {} reminders", upcomingAppointments.size());
    }
    
    private String formatAppointmentDetails(Appointment appointment) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy 'at' hh:mm a");
        return String.format(
            "Service: %s\nDate & Time: %s\nDuration: %d minutes\nProvider: %s\nStatus: %s%s",
            appointment.getServiceType(),
            appointment.getAppointmentDate().format(formatter),
            appointment.getDurationMinutes(),
            appointment.getServiceProvider().getFullName(),
            appointment.getStatus(),
            appointment.getNotes() != null ? "\nNotes: " + appointment.getNotes() : ""
        );
    }
}

