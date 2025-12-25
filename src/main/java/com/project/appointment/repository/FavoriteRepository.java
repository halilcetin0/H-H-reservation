package com.project.appointment.repository;

import com.project.appointment.entity.Favorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    
    Optional<Favorite> findByUserIdAndBusinessId(Long userId, Long businessId);
    
    boolean existsByUserIdAndBusinessId(Long userId, Long businessId);
    
    Page<Favorite> findByUserId(Long userId, Pageable pageable);
    
    java.util.List<Favorite> findByUserId(Long userId);
    
    void deleteByUserIdAndBusinessId(Long userId, Long businessId);
    
    @Query("SELECT COUNT(f) FROM Favorite f WHERE f.business.id = :businessId")
    Long countByBusinessId(@Param("businessId") Long businessId);
}

