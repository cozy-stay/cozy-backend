package com.cozystay.dto.service;

import com.cozystay.model.PricingUnit;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateServiceRequest {

    @Size(min = 3, max = 100, message = "Title must be between 3 and 100 characters")
    private String title;

    @Size(max = 1000, message = "Description must be less than 1000 characters")
    private String description;

    @Positive(message = "Price must be positive")
    private BigDecimal price;

    private PricingUnit pricingUnit;

    @Positive(message = "Capacity must be positive")
    private Integer capacity;

    @Size(max = 255, message = "Address must be less than 255 characters")
    private String address;

    private Double latitude;

    private Double longitude;

    private Set<String> amenities;

    private Set<String> policies;

    private List<String> images;

    private String thumbnailUrl;

    private Long categoryId;
}