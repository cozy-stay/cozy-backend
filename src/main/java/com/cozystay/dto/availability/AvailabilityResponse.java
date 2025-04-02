package com.cozystay.dto.availability;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AvailabilityResponse {

    private Long id;
    private Long serviceId;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private boolean isAvailable;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}