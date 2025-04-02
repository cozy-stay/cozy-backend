package com.cozystay.dto.location;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LocationResponse {

    private Long id;
    private String city;
    private String region;
    private String country;
    private Double latitude;
    private Double longitude;
    private String description;
    private String imageUrl;
    private boolean isPopular;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}