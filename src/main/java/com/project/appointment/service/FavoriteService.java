package com.project.appointment.service;

import com.project.appointment.dto.response.BusinessResponse;
import com.project.appointment.entity.Business;
import com.project.appointment.entity.Favorite;
import com.project.appointment.entity.User;
import com.project.appointment.exception.BusinessException;
import com.project.appointment.exception.ResourceNotFoundException;
import com.project.appointment.repository.BusinessRepository;
import com.project.appointment.repository.FavoriteRepository;
import com.project.appointment.repository.ReviewRepository;
import com.project.appointment.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FavoriteService {
    
    private final FavoriteRepository favoriteRepository;
    private final BusinessRepository businessRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    
    @Transactional
    public void addFavorite(Long businessId, Long userId) {
        // Check if business exists
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Business not found"));
        
        // Check if already favorited
        if (favoriteRepository.existsByUserIdAndBusinessId(userId, businessId)) {
            throw new BusinessException("Business already in favorites");
        }
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        Favorite favorite = Favorite.builder()
                .user(user)
                .business(business)
                .build();
        
        favoriteRepository.save(favorite);
        log.info("Business {} added to favorites by user {}", businessId, userId);
    }
    
    @Transactional
    public void removeFavorite(Long businessId, Long userId) {
        Favorite favorite = favoriteRepository.findByUserIdAndBusinessId(userId, businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Favorite not found"));
        
        favoriteRepository.delete(favorite);
        log.info("Business {} removed from favorites by user {}", businessId, userId);
    }
    
    public List<BusinessResponse> getUserFavorites(Long userId) {
        return favoriteRepository.findByUserId(userId)
                .stream()
                .map(favorite -> mapBusinessToResponse(favorite.getBusiness()))
                .collect(Collectors.toList());
    }
    
    public boolean isFavorite(Long businessId, Long userId) {
        return favoriteRepository.existsByUserIdAndBusinessId(userId, businessId);
    }
    
    public Long getFavoriteCount(Long businessId) {
        return favoriteRepository.countByBusinessId(businessId);
    }
    
    private BusinessResponse mapBusinessToResponse(Business business) {
        Double avgRating = reviewRepository.getAverageRatingByBusinessId(business.getId());
        Long favoriteCount = favoriteRepository.countByBusinessId(business.getId());
        
        return BusinessResponse.builder()
                .id(business.getId())
                .ownerId(business.getOwnerId())
                .name(business.getName())
                .description(business.getDescription())
                .category(business.getCategory())
                .address(business.getAddress())
                .city(business.getCity())
                .businessType(business.getBusinessType())
                .phone(business.getPhone())
                .email(business.getEmail())
                .imageUrl(business.getImageUrl())
                .isActive(business.getIsActive())
                .averageRating(avgRating)
                .favoriteCount(favoriteCount)
                .createdAt(business.getCreatedAt())
                .updatedAt(business.getUpdatedAt())
                .build();
    }
}

