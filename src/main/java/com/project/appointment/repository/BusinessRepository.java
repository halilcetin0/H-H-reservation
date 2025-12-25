package com.project.appointment.repository;

import com.project.appointment.entity.Business;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BusinessRepository extends JpaRepository<Business, Long> {
    
    Optional<Business> findByOwnerId(Long ownerId);
    
    boolean existsByOwnerId(Long ownerId);
    
    Page<Business> findByIsActiveTrue(Pageable pageable);
    
    Page<Business> findByCityAndIsActiveTrue(String city, Pageable pageable);
    
    Page<Business> findByCategoryAndIsActiveTrue(String category, Pageable pageable);
    
    @Query("SELECT b FROM Business b WHERE " +
           "(LOWER(b.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(b.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND b.isActive = true")
    Page<Business> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
}

