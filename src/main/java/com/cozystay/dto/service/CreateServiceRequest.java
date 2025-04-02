package com.cozystay.dto.service;

import com.cozystay.model.PricingUnit;
import com.cozystay.model.ServiceType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateServiceRequest {

    @NotBlank(message = "Title cannot be blank")
    @Size(min = 3, max = 100, message = "Title must be between 3 and 100 characters")
    private String title;

    @NotBlank(message = "Description cannot be blank")
    @Size(max = 1000, message = "Description must be less than 1000 characters")
    private String description;

    @NotNull(message = "Service type cannot be null")
    private ServiceType type;

    @NotNull(message = "Price cannot be null")
    @Positive(message = "Price must be positive")
    private BigDecimal price;

    @NotNull(message = "Pricing unit cannot be null")
    private PricingUnit pricingUnit;

    @Positive(message = "Capacity must be positive")
    private Integer capacity;

    @Size(max = 255, message = "Address must be less than 255 characters")
    private String address;

    @NotNull(message = "Latitude cannot be null")
    private Double latitude;

    @NotNull(message = "Longitude cannot be null")
    private Double longitude;

    private Set<String> amenities;

    private Set<String> policies;

    private List<String> images = new ArrayList<>();

    private String thumbnailUrl;

    @NotNull(message = "Category ID cannot be null")
    private Long categoryId;

    @NotNull(message = "Location ID cannot be null")
    private Long locationId;
}