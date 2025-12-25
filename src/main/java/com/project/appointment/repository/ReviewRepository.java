package com.project.appointment.repository;

import com.project.appointment.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    
    Optional<Review> findByAppointmentId(Long appointmentId);
    
    boolean existsByAppointmentId(Long appointmentId);
    
    Page<Review> findByBusinessId(Long businessId, Pageable pageable);
    
    Page<Review> findByEmployeeId(Long employeeId, Pageable pageable);
    
    Long countByBusinessId(Long businessId);
    
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.business.id = :businessId")
    Double getAverageRatingByBusinessId(@Param("businessId") Long businessId);
    
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.employee.id = :employeeId")
    Double getAverageRatingByEmployeeId(@Param("employeeId") Long employeeId);
}

