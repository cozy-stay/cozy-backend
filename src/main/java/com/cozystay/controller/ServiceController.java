package com.cozystay.controller;

import com.cozystay.dto.service.CreateServiceRequest;
import com.cozystay.dto.service.ServiceDetailResponse;
import com.cozystay.dto.service.ServiceResponse;
import com.cozystay.dto.service.UpdateServiceRequest;
import com.cozystay.model.ServiceType;
import com.cozystay.service.ServiceManagementService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/services")
public class ServiceController {

    @Autowired
    private ServiceManagementService serviceManagementService;

    @GetMapping
    public ResponseEntity<Page<ServiceResponse>> getAllServices(
            @PageableDefault(size = 10) Pageable pageable) {
        Page<ServiceResponse> services = serviceManagementService.getAllServices(pageable);
        return ResponseEntity.ok(services);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceDetailResponse> getServiceById(@PathVariable Long id) {
        ServiceDetailResponse service = serviceManagementService.getServiceById(id);
        return ResponseEntity.ok(service);
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<Page<ServiceResponse>> getServicesByType(
            @PathVariable ServiceType type,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<ServiceResponse> services = serviceManagementService.getServicesByType(type, pageable);
        return ResponseEntity.ok(services);
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<Page<ServiceResponse>> getServicesByCategory(
            @PathVariable Long categoryId,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<ServiceResponse> services = serviceManagementService.getServicesByCategory(categoryId, pageable);
        return ResponseEntity.ok(services);
    }

    @GetMapping("/location/{locationId}")
    public ResponseEntity<Page<ServiceResponse>> getServicesByLocation(
            @PathVariable Long locationId,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<ServiceResponse> services = serviceManagementService.getServicesByLocation(locationId, pageable);
        return ResponseEntity.ok(services);
    }

    @GetMapping("/price")
    public ResponseEntity<Page<ServiceResponse>> getServicesByPriceRange(
            @RequestParam BigDecimal minPrice,
            @RequestParam BigDecimal maxPrice,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<ServiceResponse> services = serviceManagementService.getServicesByPriceRange(minPrice, maxPrice, pageable);
        return ResponseEntity.ok(services);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ServiceResponse>> searchServices(
            @RequestParam String keyword,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<ServiceResponse> services = serviceManagementService.searchServices(keyword, pageable);
        return ResponseEntity.ok(services);
    }

    @GetMapping("/provider/{providerId}")
    public ResponseEntity<Page<ServiceResponse>> getServicesByProvider(
            @PathVariable Long providerId,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<ServiceResponse> services = serviceManagementService.getServicesByProvider(providerId, pageable);
        return ResponseEntity.ok(services);
    }

    @GetMapping("/top-rated")
    public ResponseEntity<Page<ServiceResponse>> getTopRatedServices(
            @PageableDefault(size = 10) Pageable pageable) {
        Page<ServiceResponse> services = serviceManagementService.getTopRatedServices(pageable);
        return ResponseEntity.ok(services);
    }

    @GetMapping("/popular")
    public ResponseEntity<List<ServiceResponse>> getMostPopularServices(
            @PageableDefault(size = 10) Pageable pageable) {
        List<ServiceResponse> services = serviceManagementService.getMostPopularServices(pageable);
        return ResponseEntity.ok(services);
    }

    @PostMapping
    @PreAuthorize("hasRole('PROVIDER') or hasRole('ADMIN')")
    public ResponseEntity<ServiceDetailResponse> createService(
            @RequestPart("dto") String createRequestDto,
            @RequestPart("images") List<MultipartFile> images
            ) throws IOException {

        ObjectMapper objectMapper = new ObjectMapper();
        CreateServiceRequest createRequest = objectMapper.readValue(createRequestDto, CreateServiceRequest.class);

        System.out.println(images);
        System.out.println(createRequest);

        ServiceDetailResponse service = serviceManagementService.createService(createRequest, images);
        return new ResponseEntity<>(service, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServiceDetailResponse> updateService(
            @PathVariable Long id,
            @Valid @RequestBody UpdateServiceRequest updateRequest) {
        ServiceDetailResponse service = serviceManagementService.updateService(id, updateRequest);
        return ResponseEntity.ok(service);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteService(@PathVariable Long id) {
        serviceManagementService.deleteService(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ServiceDetailResponse> toggleServiceActiveStatus(
            @PathVariable Long id,
            @RequestParam boolean isActive) {
        ServiceDetailResponse service = serviceManagementService.toggleServiceActiveStatus(id, isActive);
        return ResponseEntity.ok(service);
    }

    @PatchMapping("/{id}/verify")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ServiceDetailResponse> verifyService(@PathVariable Long id) {
        ServiceDetailResponse service = serviceManagementService.verifyService(id);
        return ResponseEntity.ok(service);
    }
}