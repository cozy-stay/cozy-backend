package com.cozystay.controller;

import com.cozystay.dto.availability.AvailabilityRequest;
import com.cozystay.dto.availability.AvailabilityResponse;
import com.cozystay.service.AvailabilityService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/availabilities")
public class AvailabilityController {

    @Autowired
    private AvailabilityService availabilityService;

    @GetMapping("/service/{serviceId}")
    public ResponseEntity<List<AvailabilityResponse>> getAvailabilitiesByServiceId(
            @PathVariable Long serviceId) {
        List<AvailabilityResponse> availabilities = availabilityService.getAvailabilitiesByServiceId(serviceId);
        return ResponseEntity.ok(availabilities);
    }

    @GetMapping("/service/{serviceId}/dates")
    public ResponseEntity<List<AvailabilityResponse>> getAvailabilitiesByServiceIdBetweenDates(
            @PathVariable Long serviceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<AvailabilityResponse> availabilities = availabilityService
                .getAvailabilitiesByServiceIdBetweenDates(serviceId, startDate, endDate);
        return ResponseEntity.ok(availabilities);
    }

    @GetMapping("/service/{serviceId}/available")
    public ResponseEntity<List<AvailabilityResponse>> getAvailableDatesByServiceId(
            @PathVariable Long serviceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<AvailabilityResponse> availabilities = availabilityService
                .getAvailableDatesByServiceId(serviceId, startDate, endDate);
        return ResponseEntity.ok(availabilities);
    }

    @PostMapping
    public ResponseEntity<AvailabilityResponse> createAvailability(
            @Valid @RequestBody AvailabilityRequest request) {
        AvailabilityResponse availability = availabilityService.createAvailability(request);
        return new ResponseEntity<>(availability, HttpStatus.CREATED);
    }

    @PostMapping("/bulk")
    public ResponseEntity<List<AvailabilityResponse>> createBulkAvailabilities(
            @Valid @RequestBody List<AvailabilityRequest> requests) {
        List<AvailabilityResponse> availabilities = availabilityService.createBulkAvailabilities(requests);
        return new ResponseEntity<>(availabilities, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AvailabilityResponse> updateAvailability(
            @PathVariable Long id,
            @Valid @RequestBody AvailabilityRequest request) {
        AvailabilityResponse availability = availabilityService.updateAvailability(id, request);
        return ResponseEntity.ok(availability);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAvailability(@PathVariable Long id) {
        availabilityService.deleteAvailability(id);
        return ResponseEntity.noContent().build();
    }
}