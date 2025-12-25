package com.project.appointment.service;

import com.project.appointment.dto.request.BusinessRequest;
import com.project.appointment.dto.response.BusinessResponse;
import com.project.appointment.entity.Business;
import com.project.appointment.entity.BusinessType;
import com.project.appointment.exception.BusinessException;
import com.project.appointment.repository.BusinessRepository;
import com.project.appointment.repository.FavoriteRepository;
import com.project.appointment.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BusinessService {
    
    private final BusinessRepository businessRepository;
    private final FavoriteRepository favoriteRepository;
    private final ReviewRepository reviewRepository;
    
    @Transactional
    @CacheEvict(value = "businesses", allEntries = true)
    public BusinessResponse createBusiness(Long ownerId, BusinessRequest request) {
        // Check if user already has a business
        if (businessRepository.existsByOwnerId(ownerId)) {
            throw new BusinessException("You already have a business");
        }
        
        Business business = Business.builder()
                .ownerId(ownerId)
                .name(request.getName())
                .description(request.getDescription())
                .category(request.getCategory())
                .address(request.getAddress())
                .city(request.getCity())
                .businessType(request.getBusinessType() != null ? 
                    BusinessType.valueOf(request.getBusinessType()) : null)
                .phone(request.getPhone())
                .email(request.getEmail())
                .imageUrl(request.getImageUrl())
                .build();
        
        business = businessRepository.save(business);
        log.info("Business created: {} by owner: {}", business.getId(), ownerId);
        
        return mapToResponse(business);
    }
    
    @Cacheable(value = "businesses", key = "'all:' + #pageable.pageNumber")
    public Page<BusinessResponse> getAllBusinesses(Pageable pageable) {
        return businessRepository.findByIsActiveTrue(pageable)
                .map(this::mapToResponse);
    }
    
    @Cacheable(value = "business", key = "#id")
    public BusinessResponse getBusinessById(Long id) {
        Business business = businessRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Business not found"));
        return mapToResponse(business);
    }
    
    public BusinessResponse getMyBusiness(Long ownerId) {
        Business business = businessRepository.findByOwnerId(ownerId)
                .orElseThrow(() -> new BusinessException("You don't have a business yet"));
        return mapToResponse(business);
    }
    
    @Transactional
    @CacheEvict(value = {"business", "businesses"}, allEntries = true)
    public BusinessResponse updateBusiness(Long ownerId, BusinessRequest request) {
        Business business = businessRepository.findByOwnerId(ownerId)
                .orElseThrow(() -> new BusinessException("Business not found"));
        
        // Update only non-null fields (partial update)
        if (request.getName() != null) business.setName(request.getName());
        if (request.getDescription() != null) business.setDescription(request.getDescription());
        if (request.getCategory() != null) business.setCategory(request.getCategory());
        if (request.getAddress() != null) business.setAddress(request.getAddress());
        if (request.getCity() != null) business.setCity(request.getCity());
        if (request.getBusinessType() != null) {
            business.setBusinessType(BusinessType.valueOf(request.getBusinessType()));
        }
        if (request.getPhone() != null) business.setPhone(request.getPhone());
        if (request.getEmail() != null) business.setEmail(request.getEmail());
        if (request.getImageUrl() != null) business.setImageUrl(request.getImageUrl());
        
        business = businessRepository.save(business);
        log.info("Business updated: {}", business.getId());
        
        return mapToResponse(business);
    }
    
    public Page<BusinessResponse> searchBusinesses(String keyword, Pageable pageable) {
        return businessRepository.searchByKeyword(keyword, pageable)
                .map(this::mapToResponse);
    }
    
    private BusinessResponse mapToResponse(Business business) {
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

