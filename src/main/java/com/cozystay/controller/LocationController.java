package com.cozystay.controller;

import com.cozystay.dto.location.LocationRequest;
import com.cozystay.dto.location.LocationResponse;
import com.cozystay.service.LocationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/locations")
public class LocationController {

    @Autowired
    private LocationService locationService;

    @GetMapping
    public ResponseEntity<List<LocationResponse>> getAllLocations() {
        List<LocationResponse> locations = locationService.getAllLocations();
        return ResponseEntity.ok(locations);
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<LocationResponse>> getAllLocationsAdmin() {
        List<LocationResponse> locations = locationService.getAllLocationsAdmin();
        return ResponseEntity.ok(locations);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LocationResponse> getLocationById(@PathVariable Long id) {
        LocationResponse location = locationService.getLocationById(id);
        return ResponseEntity.ok(location);
    }

    @GetMapping("/popular")
    public ResponseEntity<List<LocationResponse>> getPopularLocations() {
        List<LocationResponse> locations = locationService.getPopularLocations();
        return ResponseEntity.ok(locations);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<LocationResponse>> searchLocations(
            @RequestParam String keyword,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<LocationResponse> locations = locationService.searchLocations(keyword, pageable);
        return ResponseEntity.ok(locations);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LocationResponse> createLocation(
            @Valid @RequestBody LocationRequest locationRequest) {
        LocationResponse location = locationService.createLocation(locationRequest);
        return new ResponseEntity<>(location, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LocationResponse> updateLocation(
            @PathVariable Long id,
            @Valid @RequestBody LocationRequest locationRequest) {
        LocationResponse location = locationService.updateLocation(id, locationRequest);
        return ResponseEntity.ok(location);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LocationResponse> toggleLocationActiveStatus(
            @PathVariable Long id,
            @RequestParam boolean isActive) {
        LocationResponse location = locationService.toggleLocationActiveStatus(id, isActive);
        return ResponseEntity.ok(location);
    }

    @PatchMapping("/{id}/popular")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LocationResponse> toggleLocationPopularStatus(
            @PathVariable Long id,
            @RequestParam boolean isPopular) {
        LocationResponse location = locationService.toggleLocationPopularStatus(id, isPopular);
        return ResponseEntity.ok(location);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteLocation(@PathVariable Long id) {
        locationService.deleteLocation(id);
        return ResponseEntity.noContent().build();
    }
}