package com.cozystay.service;

import com.cloudinary.Cloudinary;
import com.cozystay.dto.service.CreateServiceRequest;
import com.cozystay.dto.service.ServiceDetailResponse;
import com.cozystay.dto.service.ServiceResponse;
import com.cozystay.dto.service.UpdateServiceRequest;
import com.cozystay.exception.ResourceNotFoundException;
import com.cozystay.exception.UnauthorizedException;
import com.cozystay.model.*;
import com.cozystay.model.Service;
import com.cozystay.repository.CategoryRepository;
import com.cozystay.repository.LocationRepository;
import com.cozystay.repository.ServiceRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
public class ServiceManagementService {

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private final Cloudinary cloudinary;

    public ServiceManagementService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public Page<ServiceResponse> getAllServices(Pageable pageable) {
        Page<Service> services = serviceRepository.findByIsActiveTrue(pageable);
        return services.map(service -> modelMapper.map(service, ServiceResponse.class));
    }

    public ServiceDetailResponse getServiceById(Long id) {
        Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found with id: " + id));
        return modelMapper.map(service, ServiceDetailResponse.class);
    }

    public Page<ServiceResponse> getServicesByType(ServiceType type, Pageable pageable) {
        Page<Service> services = serviceRepository.findByTypeAndIsActiveTrue(type, pageable);
        return services.map(service -> modelMapper.map(service, ServiceResponse.class));
    }

    public Page<ServiceResponse> getServicesByCategory(Long categoryId, Pageable pageable) {
        // Verify category exists
        categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));

        Page<Service> services = serviceRepository.findByCategoryIdAndIsActiveTrue(categoryId, pageable);
        return services.map(service -> modelMapper.map(service, ServiceResponse.class));
    }

    public Page<ServiceResponse> getServicesByLocation(Long locationId, Pageable pageable) {
        // Verify location exists
        locationRepository.findById(locationId)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found with id: " + locationId));

        Page<Service> services = serviceRepository.findByLocationIdAndIsActiveTrue(locationId, pageable);
        return services.map(service -> modelMapper.map(service, ServiceResponse.class));
    }

    public Page<ServiceResponse> getServicesByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        Page<Service> services = serviceRepository.findByPriceRange(minPrice, maxPrice, pageable);
        return services.map(service -> modelMapper.map(service, ServiceResponse.class));
    }

    public Page<ServiceResponse> searchServices(String keyword, Pageable pageable) {
        Page<Service> services = serviceRepository.searchByKeyword(keyword, pageable);
        return services.map(service -> modelMapper.map(service, ServiceResponse.class));
    }

    public Page<ServiceResponse> getServicesByProvider(Long providerId, Pageable pageable) {
        Page<Service> services = serviceRepository.findByUserId(providerId, pageable);
        return services.map(service -> modelMapper.map(service, ServiceResponse.class));
    }

    public Page<ServiceResponse> getTopRatedServices(Pageable pageable) {
        Page<Service> services = serviceRepository.findTopRatedServices(pageable);
        return services.map(service -> modelMapper.map(service, ServiceResponse.class));
    }

    public List<ServiceResponse> getMostPopularServices(Pageable pageable) {
        List<Service> services = serviceRepository.findMostPopularServices(pageable);
        return services.stream()
                .map(service -> modelMapper.map(service, ServiceResponse.class))
                .collect(Collectors.toList());
    }

    @Transactional
    public ServiceDetailResponse createService(CreateServiceRequest createRequest, List<MultipartFile> images) throws IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        // Only providers or admins can create services
        if (!currentUser.getAuthorities().stream().anyMatch(a ->
                a.getAuthority().equals("ROLE_PROVIDER") || a.getAuthority().equals("ROLE_ADMIN"))) {
            throw new UnauthorizedException("You don't have permission to create services");
        }

        Category category = categoryRepository.findById(createRequest.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + createRequest.getCategoryId()));

        Location location = locationRepository.findById(createRequest.getLocationId())
                .orElseThrow(() -> new ResourceNotFoundException("Location not found with id: " + createRequest.getLocationId()));

        for (MultipartFile image : images) {
            String imageUrl = uploadDocumentToCloudinary(image);
            createRequest.getImages().add(imageUrl);
        }

        Service service = Service.builder()
                .user(currentUser)
                .title(createRequest.getTitle())
                .description(createRequest.getDescription())
                .type(createRequest.getType())
                .price(createRequest.getPrice())
                .pricingUnit(createRequest.getPricingUnit())
                .capacity(createRequest.getCapacity())
                .address(createRequest.getAddress())
                .latitude(createRequest.getLatitude())
                .longitude(createRequest.getLongitude())
                .amenities(new HashSet<>(createRequest.getAmenities()))
                .policies(new HashSet<>(createRequest.getPolicies()))
                .images(new ArrayList<>(createRequest.getImages()))
                .thumbnailUrl(createRequest.getImages().get(0))
                .category(category)
                .location(location)
                .isActive(true)
                .isVerified(false)
                .avgRating(0.0)
                .reviewCount(0)
                .build();

        Service savedService = serviceRepository.save(service);
        return modelMapper.map(savedService, ServiceDetailResponse.class);
    }

    @Transactional
    public ServiceDetailResponse updateService(Long id, UpdateServiceRequest updateRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found with id: " + id));

        // Only the owner or admins can update a service
        if (!service.getUser().getId().equals(currentUser.getId()) &&
                !currentUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            throw new UnauthorizedException("You don't have permission to update this service");
        }

        if (updateRequest.getTitle() != null) {
            service.setTitle(updateRequest.getTitle());
        }

        if (updateRequest.getDescription() != null) {
            service.setDescription(updateRequest.getDescription());
        }

        if (updateRequest.getPrice() != null) {
            service.setPrice(updateRequest.getPrice());
        }

        if (updateRequest.getPricingUnit() != null) {
            service.setPricingUnit(updateRequest.getPricingUnit());
        }

        if (updateRequest.getCapacity() != null) {
            service.setCapacity(updateRequest.getCapacity());
        }

        if (updateRequest.getAddress() != null) {
            service.setAddress(updateRequest.getAddress());
        }

        if (updateRequest.getLatitude() != null) {
            service.setLatitude(updateRequest.getLatitude());
        }

        if (updateRequest.getLongitude() != null) {
            service.setLongitude(updateRequest.getLongitude());
        }

        if (updateRequest.getAmenities() != null) {
            service.setAmenities(new HashSet<>(updateRequest.getAmenities()));
        }

        if (updateRequest.getPolicies() != null) {
            service.setPolicies(new HashSet<>(updateRequest.getPolicies()));
        }

        if (updateRequest.getImages() != null) {
            service.setImages(new ArrayList<>(updateRequest.getImages()));
        }

        if (updateRequest.getThumbnailUrl() != null) {
            service.setThumbnailUrl(updateRequest.getThumbnailUrl());
        }

        if (updateRequest.getCategoryId() != null) {
            Category category = categoryRepository.findById(updateRequest.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + updateRequest.getCategoryId()));
            service.setCategory(category);
        }

        Service updatedService = serviceRepository.save(service);
        return modelMapper.map(updatedService, ServiceDetailResponse.class);
    }

    @Transactional
    public void deleteService(Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found with id: " + id));

        // Only the owner or admins can delete a service
        if (!service.getUser().getId().equals(currentUser.getId()) &&
                !currentUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            throw new UnauthorizedException("You don't have permission to delete this service");
        }

        serviceRepository.delete(service);
    }

    @Transactional
    public ServiceDetailResponse toggleServiceActiveStatus(Long id, boolean isActive) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found with id: " + id));

        // Only the owner or admins can toggle service status
        if (!service.getUser().getId().equals(currentUser.getId()) &&
                !currentUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            throw new UnauthorizedException("You don't have permission to toggle this service status");
        }

        service.setActive(isActive);
        Service updatedService = serviceRepository.save(service);
        return modelMapper.map(updatedService, ServiceDetailResponse.class);
    }

    @Transactional
    public ServiceDetailResponse verifyService(Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        // Only admins can verify services
        if (!currentUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            throw new UnauthorizedException("You don't have permission to verify services");
        }

        Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found with id: " + id));

        service.setVerified(true);
        Service updatedService = serviceRepository.save(service);
        return modelMapper.map(updatedService, ServiceDetailResponse.class);
    }

    private String uploadDocumentToCloudinary(MultipartFile documentFile) throws IOException {
        // Extract the original filename
        String originalFilename = documentFile.getOriginalFilename();

        if (originalFilename == null) {
            throw new IOException("Invalid file: missing original filename");
        }

        // Remove potential path information and ensure a clean filename
        String cleanedFilename = Paths.get(originalFilename).getFileName().toString();
        Map<String, Object> options = Map.of(
                "resource_type", "raw",
                "folder", "services-images",
                "public_id", cleanedFilename
        );

        // Upload the file to Cloudinary
        Map<String, Object> uploadResult = cloudinary.uploader().upload(documentFile.getBytes(), options);

        // Return the accessible URL
        return (String) uploadResult.get("secure_url");
    }
}