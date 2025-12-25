package com.project.appointment.repository;

import com.project.appointment.entity.InvitationStatus;
import com.project.appointment.entity.StaffInvitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StaffInvitationRepository extends JpaRepository<StaffInvitation, Long> {
    
    Optional<StaffInvitation> findByToken(String token);
    
    Optional<StaffInvitation> findByBusinessIdAndEmailAndStatus(Long businessId, String email, InvitationStatus status);
    
    List<StaffInvitation> findByBusinessId(Long businessId);
    
    List<StaffInvitation> findByBusinessIdAndStatus(Long businessId, InvitationStatus status);
    
    boolean existsByEmailAndStatus(String email, InvitationStatus status);
}
