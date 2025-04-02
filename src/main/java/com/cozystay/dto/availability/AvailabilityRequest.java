package com.cozystay.dto.availability;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AvailabilityRequest {

    @NotNull(message = "Service ID cannot be null")
    private Long serviceId;

    @NotNull(message = "Start date cannot be null")
    private LocalDateTime startDateTime;

    @NotNull(message = "End date cannot be null")
    private LocalDateTime endDateTime;

    private boolean isAvailable = true;

    private String notes;
}