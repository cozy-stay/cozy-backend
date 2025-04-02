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

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceResponse {

    private Long id;
    private String title;
    private ServiceType type;
    private BigDecimal price;
    private PricingUnit pricingUnit;
    private Integer capacity;
    private String thumbnailUrl;
    private String address;
    private Double latitude;
    private Double longitude;
    private Double avgRating;
    private Integer reviewCount;
    private boolean isVerified;
    private CategoryResponse category;
    private LocationResponse location;
    private UserResponse user;
}