package com.project.appointment.repository;

import com.project.appointment.entity.Appointment;
import com.project.appointment.entity.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByUserId(Long userId);
    List<Appointment> findByServiceProviderId(Long serviceProviderId);
    List<Appointment> findByStatus(AppointmentStatus status);
    List<Appointment> findByAppointmentDateBetween(LocalDateTime start, LocalDateTime end);
    List<Appointment> findByReminderSentFalseAndAppointmentDateBetween(LocalDateTime start, LocalDateTime end);
}

