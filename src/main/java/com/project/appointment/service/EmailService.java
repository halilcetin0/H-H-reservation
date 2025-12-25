package com.project.appointment.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    
    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;
    
    @Value("${app.email.from}")
    private String fromEmail;
    
    @Value("${app.name}")
    private String appName;
    
    @Async
    public void sendEmail(String to, String subject, String templateName, Map<String, Object> variables) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            
            Context context = new Context();
            context.setVariables(variables);
            context.setVariable("appName", appName);
            
            String html = templateEngine.process(templateName, context);
            helper.setText(html, true);
            
            mailSender.send(message);
            log.info("Email sent successfully to: {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send email to: {}", to, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }
    
    public void sendVerificationEmail(String to, String fullName, String verificationLink) {
        Map<String, Object> variables = Map.of(
            "fullName", fullName,
            "verificationLink", verificationLink
        );
        sendEmail(to, "Verify Your Email Address", "email-verification", variables);
    }
    
    public void sendPasswordResetEmail(String to, String fullName, String resetLink) {
        Map<String, Object> variables = Map.of(
            "fullName", fullName,
            "resetLink", resetLink
        );
        sendEmail(to, "Reset Your Password", "password-reset", variables);
    }
    
    public void sendAppointmentConfirmationEmail(String to, String fullName, String appointmentDetails) {
        Map<String, Object> variables = Map.of(
            "fullName", fullName,
            "appointmentDetails", appointmentDetails
        );
        sendEmail(to, "Appointment Confirmation", "appointment-confirmation", variables);
    }
    
    public void sendAppointmentReminderEmail(String to, String fullName, String appointmentDetails) {
        Map<String, Object> variables = Map.of(
            "fullName", fullName,
            "appointmentDetails", appointmentDetails
        );
        sendEmail(to, "Appointment Reminder", "appointment-reminder", variables);
    }
    
    public void sendAppointmentCancellationEmail(String to, String fullName, String appointmentDetails, String reason) {
        Map<String, Object> variables = Map.of(
            "fullName", fullName,
            "appointmentDetails", appointmentDetails,
            "reason", reason != null ? reason : "No reason provided"
        );
        sendEmail(to, "Appointment Cancelled", "appointment-cancellation", variables);
    }
}

