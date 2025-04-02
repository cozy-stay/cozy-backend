package com.cozystay.service;

import com.cozystay.dto.location.LocationRequest;
import com.cozystay.dto.location.LocationResponse;
import com.cozystay.exception.ResourceAlreadyExistsException;
import com.cozystay.exception.ResourceNotFoundException;
import com.cozystay.exception.UnauthorizedException;
import com.cozystay.model.Location;
import com.cozystay.model.User;
import com.cozystay.repository.LocationRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class LocationService {

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private ModelMapper modelMapper;

    public List<LocationResponse> getAllLocations() {
        List<Location> locations = locationRepository.findByIsActiveTrue();
        return locations.stream()
                .map(location -> modelMapper.map(location, LocationResponse.class))
                .collect(Collectors.toList());
    }

    public List<LocationResponse> getAllLocationsAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        // Only admins can view all locations including inactive ones
        if (!currentUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            throw new UnauthorizedException("You don't have permission to view all locations");
        }

        List<Location> locations = locationRepository.findAll();
        return locations.stream()
                .map(location -> modelMapper.map(location, LocationResponse.class))
                .collect(Collectors.toList());
    }

    public LocationResponse getLocationById(Long id) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found with id: " + id));
        return modelMapper.map(location, LocationResponse.class);
    }

    public List<LocationResponse> getPopularLocations() {
        List<Location> locations = locationRepository.findByIsPopularAndIsActiveTrue(true);
        return locations.stream()
                .map(location -> modelMapper.map(location, LocationResponse.class))
                .collect(Collectors.toList());
    }

    public Page<LocationResponse> searchLocations(String keyword, Pageable pageable) {
        Page<Location> locations = locationRepository.searchByKeyword(keyword, pageable);
        return locations.map(location -> modelMapper.map(location, LocationResponse.class));
    }

    @Transactional
    public LocationResponse createLocation(LocationRequest locationRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        // Only admins can create locations
        if (!currentUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            throw new UnauthorizedException("You don't have permission to create locations");
        }

        // Check if location with same city, region, country already exists
        Optional<Location> existingLocation = locationRepository.findByCityIgnoreCaseAndRegionIgnoreCaseAndCountryIgnoreCase(
                locationRequest.getCity(), locationRequest.getRegion(), locationRequest.getCountry());

        if (existingLocation.isPresent()) {
            throw new ResourceAlreadyExistsException("Location already exists");
        }

        Location location = Location.builder()
                .city(locationRequest.getCity())
                .region(locationRequest.getRegion())
                .country(locationRequest.getCountry())
                .latitude(locationRequest.getLatitude())
                .longitude(locationRequest.getLongitude())
                .description(locationRequest.getDescription())
                .imageUrl(locationRequest.getImageUrl())
                .isPopular(locationRequest.isPopular())
                .isActive(true)
                .build();

        Location savedLocation = locationRepository.save(location);
        return modelMapper.map(savedLocation, LocationResponse.class);
    }

    @Transactional
    public LocationResponse updateLocation(Long id, LocationRequest locationRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        // Only admins can update locations
        if (!currentUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            throw new UnauthorizedException("You don't have permission to update locations");
        }

        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found with id: " + id));

        // Check if location details are being changed and if it already exists
        if (locationRequest.getCity() != null && locationRequest.getRegion() != null && locationRequest.getCountry() != null) {
            if (!location.getCity().equalsIgnoreCase(locationRequest.getCity()) ||
                    !location.getRegion().equalsIgnoreCase(locationRequest.getRegion()) ||
                    !location.getCountry().equalsIgnoreCase(locationRequest.getCountry())) {

                Optional<Location> existingLocation = locationRepository.findByCityIgnoreCaseAndRegionIgnoreCaseAndCountryIgnoreCase(
                        locationRequest.getCity(), locationRequest.getRegion(), locationRequest.getCountry());

                if (existingLocation.isPresent() && !existingLocation.get().getId().equals(id)) {
                    throw new ResourceAlreadyExistsException("Location already exists");
                }
            }
        }

        if (locationRequest.getCity() != null) {
            location.setCity(locationRequest.getCity());
        }

        if (locationRequest.getRegion() != null) {
            location.setRegion(locationRequest.getRegion());
        }

        if (locationRequest.getCountry() != null) {
            location.setCountry(locationRequest.getCountry());
        }

        if (locationRequest.getLatitude() != null) {
            location.setLatitude(locationRequest.getLatitude());
        }

        if (locationRequest.getLongitude() != null) {
            location.setLongitude(locationRequest.getLongitude());
        }

        if (locationRequest.getDescription() != null) {
            location.setDescription(locationRequest.getDescription());
        }

        if (locationRequest.getImageUrl() != null) {
            location.setImageUrl(locationRequest.getImageUrl());
        }

        if (locationRequest.isPopular() != location.isPopular()) {
            location.setPopular(locationRequest.isPopular());
        }

        Location updatedLocation = locationRepository.save(location);
        return modelMapper.map(updatedLocation, LocationResponse.class);
    }

    @Transactional
    public LocationResponse toggleLocationActiveStatus(Long id, boolean isActive) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        // Only admins can toggle location status
        if (!currentUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            throw new UnauthorizedException("You don't have permission to toggle location status");
        }

        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found with id: " + id));

        location.setActive(isActive);
        Location updatedLocation = locationRepository.save(location);
        return modelMapper.map(updatedLocation, LocationResponse.class);
    }

    @Transactional
    public LocationResponse toggleLocationPopularStatus(Long id, boolean isPopular) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        // Only admins can toggle location popular status
        if (!currentUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            throw new UnauthorizedException("You don't have permission to toggle location popular status");
        }

        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found with id: " + id));

        location.setPopular(isPopular);
        Location updatedLocation = locationRepository.save(location);
        return modelMapper.map(updatedLocation, LocationResponse.class);
    }

    @Transactional
    public void deleteLocation(Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        // Only admins can delete locations
        if (!currentUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            throw new UnauthorizedException("You don't have permission to delete locations");
        }

        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found with id: " + id));

        // Instead of deleting, mark as inactive if it has related services
        if (!location.getServices().isEmpty()) {
            location.setActive(false);
            locationRepository.save(location);
        } else {
            locationRepository.delete(location);
        }
    }
}