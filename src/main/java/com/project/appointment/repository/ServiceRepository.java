package com.project.appointment.repository;

import com.project.appointment.entity.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceRepository extends JpaRepository<Service, Long> {
    
    List<Service> findByBusinessIdAndIsActiveTrue(Long businessId);
    
    List<Service> findByBusinessId(Long businessId);
}

