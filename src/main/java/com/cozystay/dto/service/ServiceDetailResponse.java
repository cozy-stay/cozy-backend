package com.cozystay.dto.service;

import com.cozystay.dto.category.CategoryResponse;
import com.cozystay.dto.location.LocationResponse;
import com.cozystay.dto.user.UserResponse;
import com.cozystay.model.PricingUnit;
import com.cozystay.model.ServiceType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceDetailResponse {

    private Long id;
    private String title;
    private String description;
    private ServiceType type;
    private BigDecimal price;
    private PricingUnit pricingUnit;
    private Integer capacity;
    private String address;
    private Double latitude;
    private Double longitude;
    private Set<String> amenities;
    private Set<String> policies;
    private List<String> images;
    private String thumbnailUrl;
    private boolean isActive;
    private boolean isVerified;
    private Double avgRating;
    private Integer reviewCount;
    private CategoryResponse category;
    private LocationResponse location;
    private UserResponse user;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}