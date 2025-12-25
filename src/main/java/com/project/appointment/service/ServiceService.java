package com.project.appointment.service;

import com.project.appointment.dto.request.ServiceRequest;
import com.project.appointment.dto.response.ServiceResponse;
import com.project.appointment.entity.Business;
import com.project.appointment.entity.Service;
import com.project.appointment.exception.BusinessException;
import com.project.appointment.exception.ResourceNotFoundException;
import com.project.appointment.repository.BusinessRepository;
import com.project.appointment.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class ServiceService {
    
    private final ServiceRepository serviceRepository;
    private final BusinessRepository businessRepository;
    
    @Transactional
    public ServiceResponse createService(Long businessId, ServiceRequest request, Long ownerId) {
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new BusinessException("Business not found"));
        
        if (!business.getOwnerId().equals(ownerId)) {
            throw new BusinessException("You don't have permission to add services to this business");
        }
        
        Service service = Service.builder()
                .business(business)
                .name(request.getName())
                .description(request.getDescription())
                .durationMinutes(request.getDurationMinutes())
                .price(request.getPrice())
                .build();
        
        service = serviceRepository.save(service);
        log.info("Service created: {} for business: {}", service.getId(), businessId);
        
        return mapToResponse(service);
    }
    
    public List<ServiceResponse> getServicesByBusinessId(Long businessId) {
        return serviceRepository.findByBusinessIdAndIsActiveTrue(businessId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    public ServiceResponse getServiceById(Long id) {
        Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found"));
        return mapToResponse(service);
    }
    
    @Transactional
    public ServiceResponse updateService(Long serviceId, ServiceRequest request, Long ownerId) {
        Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found"));
        
        if (!service.getBusiness().getOwnerId().equals(ownerId)) {
            throw new BusinessException("You don't have permission to update this service");
        }
        
        if (request.getName() != null) service.setName(request.getName());
        if (request.getDescription() != null) service.setDescription(request.getDescription());
        if (request.getDurationMinutes() != null) service.setDurationMinutes(request.getDurationMinutes());
        if (request.getPrice() != null) service.setPrice(request.getPrice());
        
        service = serviceRepository.save(service);
        log.info("Service updated: {}", service.getId());
        
        return mapToResponse(service);
    }
    
    @Transactional
    public void deleteService(Long serviceId, Long ownerId) {
        Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found"));
        
        if (!service.getBusiness().getOwnerId().equals(ownerId)) {
            throw new BusinessException("You don't have permission to delete this service");
        }
        
        service.setIsActive(false);
        serviceRepository.save(service);
        log.info("Service deleted (soft): {}", serviceId);
    }
    
    private ServiceResponse mapToResponse(Service service) {
        return ServiceResponse.builder()
                .id(service.getId())
                .businessId(service.getBusiness().getId())
                .name(service.getName())
                .description(service.getDescription())
                .durationMinutes(service.getDurationMinutes())
                .price(service.getPrice())
                .isActive(service.getIsActive())
                .createdAt(service.getCreatedAt())
                .build();
    }
}
