package com.project.appointment.service;

import com.project.appointment.dto.request.AppointmentRequest;
import com.project.appointment.dto.request.AppointmentSearchRequest;
import com.project.appointment.dto.response.AppointmentResponse;
import com.project.appointment.dto.response.AvailableSlotResponse;
import com.project.appointment.entity.*;
import com.project.appointment.exception.BusinessException;
import com.project.appointment.exception.ResourceNotFoundException;
import com.project.appointment.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentService {
    
    private final AppointmentRepository appointmentRepository;
    private final BusinessRepository businessRepository;
    private final ServiceRepository serviceRepository;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final WorkScheduleRepository workScheduleRepository;
    private final EmailService emailService;
    
    @Transactional
    public AppointmentResponse createAppointment(AppointmentRequest request, Long customerId) {
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        Business business = businessRepository.findById(request.getBusinessId())
                .orElseThrow(() -> new ResourceNotFoundException("Business not found"));
        
        com.project.appointment.entity.Service service = serviceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Service not found"));
        
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        
        LocalDateTime endTime = request.getStartTime().plusMinutes(service.getDurationMinutes());
        
        boolean hasConflict = appointmentRepository.existsByEmployeeIdAndAppointmentTimeBetweenAndStatusNot(
                request.getEmployeeId(),
                request.getStartTime(),
                endTime,
                AppointmentStatus.CANCELLED
        );
        
        if (hasConflict) {
            throw new BusinessException("Employee has a conflicting appointment at this time");
        }
        
        DayOfWeek dayOfWeek = DayOfWeek.fromJavaTime(request.getStartTime().getDayOfWeek());
        var schedule = workScheduleRepository.findByEmployeeIdAndDayOfWeek(request.getEmployeeId(), dayOfWeek);
        
        if (schedule.isEmpty()) {
            throw new BusinessException("Employee does not work on this day");
        }
        
        LocalTime startTime = request.getStartTime().toLocalTime();
        LocalTime endTimeLocal = endTime.toLocalTime();
        
        if (startTime.isBefore(schedule.get().getStartTime()) || endTimeLocal.isAfter(schedule.get().getEndTime())) {
            throw new BusinessException("Appointment time is outside employee's work hours");
        }
        
        Appointment appointment = Appointment.builder()
                .customer(customer)
                .business(business)
                .service(service)
                .employee(employee)
                .startTime(request.getStartTime())
                .endTime(endTime)
                .price(service.getPrice())
                .status(AppointmentStatus.PENDING) // Başlangıçta PENDING - onay bekliyor
                .ownerApproved(false)
                .employeeApproved(false)
                .paymentStatus(PaymentStatus.PENDING)
                .notes(request.getNotes())
                .build();
        
        appointment = appointmentRepository.save(appointment);
        log.info("Appointment created: {} for customer: {}", appointment.getId(), customerId);
        
        try {
            String details = formatAppointmentDetails(appointment);
            emailService.sendAppointmentConfirmationEmail(
                    customer.getEmail(),
                    customer.getFullName(),
                    details
            );
        } catch (Exception e) {
            log.error("Failed to send confirmation email for appointment {}", appointment.getId(), e);
        }
        
        return mapToResponse(appointment);
    }
    
    @Transactional
    public AppointmentResponse updateAppointmentStatus(Long appointmentId, AppointmentStatus status, Long userId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));
        
        if (!appointment.getCustomer().getId().equals(userId) &&
            !appointment.getBusiness().getOwnerId().equals(userId)) {
            throw new BusinessException("You don't have permission to update this appointment");
        }
        
        AppointmentStatus oldStatus = appointment.getStatus();
        appointment.setStatus(status);
        
        if (status == AppointmentStatus.CANCELLED) {
            try {
                String details = formatAppointmentDetails(appointment);
                emailService.sendAppointmentCancellationEmail(
                        appointment.getCustomer().getEmail(),
                        appointment.getCustomer().getFullName(),
                        details,
                        appointment.getCancellationReason()
                );
            } catch (Exception e) {
                log.error("Failed to send cancellation email", e);
            }
        }
        
        appointment = appointmentRepository.save(appointment);
        log.info("Appointment {} status changed from {} to {}", appointmentId, oldStatus, status);
        
        return mapToResponse(appointment);
    }
    
    @Transactional
    public AppointmentResponse approveAppointmentByOwner(Long appointmentId, Long userId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));
        
        // Verify user is the business owner
        if (!appointment.getBusiness().getOwnerId().equals(userId)) {
            throw new BusinessException("You don't have permission to approve this appointment");
        }
        
        appointment.setOwnerApproved(true);
        
        // If both owner and employee approved, confirm the appointment
        if (appointment.getOwnerApproved() && appointment.getEmployeeApproved()) {
            appointment.setStatus(AppointmentStatus.CONFIRMED);
            try {
                String details = formatAppointmentDetails(appointment);
                emailService.sendAppointmentConfirmationEmail(
                        appointment.getCustomer().getEmail(),
                        appointment.getCustomer().getFullName(),
                        details
                );
            } catch (Exception e) {
                log.error("Failed to send confirmation email", e);
            }
        }
        
        appointment = appointmentRepository.save(appointment);
        log.info("Appointment {} approved by owner {}", appointmentId, userId);
        
        return mapToResponse(appointment);
    }
    
    @Transactional
    public AppointmentResponse approveAppointmentByEmployee(Long appointmentId, Long userId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));
        
        // Verify user is the assigned employee
        if (appointment.getEmployee() == null || appointment.getEmployee().getUser() == null) {
            throw new BusinessException("Appointment does not have an assigned employee");
        }
        
        if (!appointment.getEmployee().getUser().getId().equals(userId)) {
            throw new BusinessException("You don't have permission to approve this appointment");
        }
        
        appointment.setEmployeeApproved(true);
        
        // If both owner and employee approved, confirm the appointment
        if (appointment.getOwnerApproved() && appointment.getEmployeeApproved()) {
            appointment.setStatus(AppointmentStatus.CONFIRMED);
            try {
                String details = formatAppointmentDetails(appointment);
                emailService.sendAppointmentConfirmationEmail(
                        appointment.getCustomer().getEmail(),
                        appointment.getCustomer().getFullName(),
                        details
                );
            } catch (Exception e) {
                log.error("Failed to send confirmation email", e);
            }
        }
        
        appointment = appointmentRepository.save(appointment);
        log.info("Appointment {} approved by employee {}", appointmentId, userId);
        
        return mapToResponse(appointment);
    }
    
    public Page<AppointmentResponse> searchAppointments(AppointmentSearchRequest search, Pageable pageable) {
        return appointmentRepository.findByBusinessId(search.getBusinessId(), pageable)
                .map(this::mapToResponse);
    }
    
    public List<AvailableSlotResponse> getAvailableSlots(Long employeeId, LocalDate date, Integer durationMinutes) {
        employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        
        DayOfWeek dayOfWeek = DayOfWeek.fromJavaTime(date.getDayOfWeek());
        var scheduleOpt = workScheduleRepository.findByEmployeeIdAndDayOfWeek(employeeId, dayOfWeek);
        
        if (scheduleOpt.isEmpty()) {
            return new ArrayList<>();
        }
        
        WorkSchedule schedule = scheduleOpt.get();
        List<AvailableSlotResponse> slots = new ArrayList<>();
        
        LocalDateTime dayStart = date.atTime(schedule.getStartTime());
        LocalDateTime dayEnd = date.atTime(schedule.getEndTime());
        
        List<Appointment> existingAppointments = appointmentRepository
                .findByEmployeeIdAndAppointmentTimeBetweenAndStatusNot(
                        employeeId, dayStart, dayEnd, AppointmentStatus.CANCELLED);
        
        LocalTime currentTime = schedule.getStartTime();
        while (currentTime.plusMinutes(durationMinutes).isBefore(schedule.getEndTime()) ||
               currentTime.plusMinutes(durationMinutes).equals(schedule.getEndTime())) {
            
            LocalDateTime slotStart = date.atTime(currentTime);
            LocalDateTime slotEnd = slotStart.plusMinutes(durationMinutes);
            
            boolean isAvailable = existingAppointments.stream()
                    .noneMatch(apt -> !(apt.getEndTime().isBefore(slotStart) || apt.getStartTime().isAfter(slotEnd)));
            
            if (isAvailable && slotStart.isAfter(LocalDateTime.now())) {
                slots.add(AvailableSlotResponse.builder()
                        .startTime(slotStart)
                        .endTime(slotEnd)
                        .build());
            }
            
            currentTime = currentTime.plusMinutes(30);
        }
        
        return slots;
    }
    
    public AppointmentResponse getAppointmentById(Long appointmentId, Long userId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));
        
        // Check if user is customer, business owner, or assigned employee
        boolean isCustomer = appointment.getCustomer().getId().equals(userId);
        boolean isOwner = appointment.getBusiness().getOwnerId().equals(userId);
        boolean isEmployee = appointment.getEmployee() != null && 
                            appointment.getEmployee().getUser() != null &&
                            appointment.getEmployee().getUser().getId().equals(userId);
        
        if (!isCustomer && !isOwner && !isEmployee) {
            throw new BusinessException("You don't have permission to view this appointment");
        }
        
        return mapToResponse(appointment);
    }
    
    public List<AppointmentResponse> getUserAppointments(Long userId) {
        return appointmentRepository.findByUserId(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    public Page<AppointmentResponse> getBusinessAppointments(Long businessId, Long ownerId, Pageable pageable) {
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Business not found"));
        
        if (!business.getOwnerId().equals(ownerId)) {
            throw new BusinessException("You don't have permission to view these appointments");
        }
        
        return appointmentRepository.findByBusinessId(businessId, pageable)
                .map(this::mapToResponse);
    }
    
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void sendAppointmentReminders() {
        log.info("Running appointment reminder task");
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime twentyFourHoursLater = now.plusHours(24);
        
        List<Appointment> upcomingAppointments = appointmentRepository
                .findByReminderSentFalseAndAppointmentTimeBetween(now, twentyFourHoursLater);
        
        int sentCount = 0;
        for (Appointment appointment : upcomingAppointments) {
            if (appointment.getStatus() == AppointmentStatus.CONFIRMED) {
                try {
                    String details = formatAppointmentDetails(appointment);
                    emailService.sendAppointmentReminderEmail(
                            appointment.getCustomer().getEmail(),
                            appointment.getCustomer().getFullName(),
                            details
                    );
                    
                    appointment.setReminderSent(true);
                    appointmentRepository.save(appointment);
                    sentCount++;
                    
                    log.info("Reminder sent for appointment ID: {}", appointment.getId());
                } catch (Exception e) {
                    log.error("Failed to send reminder for appointment ID: {}", appointment.getId(), e);
                }
            }
        }
        
        log.info("Appointment reminder task completed. Sent {} reminders", sentCount);
    }
    
    private String formatAppointmentDetails(Appointment appointment) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy 'at' hh:mm a");
        return String.format(
                "Service: %s\nDate & Time: %s\nBusiness: %s\nEmployee: %s\nStatus: %s%s",
                appointment.getService().getName(),
                appointment.getStartTime().format(formatter),
                appointment.getBusiness().getName(),
                appointment.getEmployee().getName(),
                appointment.getStatus(),
                appointment.getNotes() != null ? "\nNotes: " + appointment.getNotes() : ""
        );
    }
    
    private AppointmentResponse mapToResponse(Appointment appointment) {
        AppointmentResponse.AppointmentResponseBuilder builder = AppointmentResponse.builder()
                .id(appointment.getId())
                .customerId(appointment.getCustomer().getId())
                .customerName(appointment.getCustomer().getFullName())
                .businessId(appointment.getBusiness().getId())
                .businessName(appointment.getBusiness().getName())
                .startTime(appointment.getStartTime())
                .endTime(appointment.getEndTime())
                .price(appointment.getPrice())
                .paymentStatus(appointment.getPaymentStatus())
                .status(appointment.getStatus())
                .ownerApproved(appointment.getOwnerApproved())
                .employeeApproved(appointment.getEmployeeApproved())
                .notes(appointment.getNotes())
                .cancellationReason(appointment.getCancellationReason())
                .createdAt(appointment.getCreatedAt());
        
        if (appointment.getService() != null) {
            builder.serviceId(appointment.getService().getId())
                   .serviceName(appointment.getService().getName());
        }
        
        if (appointment.getEmployee() != null) {
            builder.employeeId(appointment.getEmployee().getId())
                   .employeeName(appointment.getEmployee().getName());
        }
        
        return builder.build();
    }
}
