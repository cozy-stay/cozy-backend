package com.cozystay.service;

import com.cozystay.dto.availability.AvailabilityRequest;
import com.cozystay.dto.availability.AvailabilityResponse;
import com.cozystay.exception.BadRequestException;
import com.cozystay.exception.ResourceNotFoundException;
import com.cozystay.exception.UnauthorizedException;
import com.cozystay.model.Availability;
import com.cozystay.model.Service;
import com.cozystay.model.User;
import com.cozystay.repository.AvailabilityRepository;
import com.cozystay.repository.ServiceRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
public class AvailabilityService {

    @Autowired
    private AvailabilityRepository availabilityRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private ModelMapper modelMapper;

    public List<AvailabilityResponse> getAvailabilitiesByServiceId(Long serviceId) {
        List<Availability> availabilities = availabilityRepository.findByServiceId(serviceId);
        return availabilities.stream()
                .map(availability -> modelMapper.map(availability, AvailabilityResponse.class))
                .collect(Collectors.toList());
    }

    public List<AvailabilityResponse> getAvailabilitiesByServiceIdBetweenDates(
            Long serviceId, LocalDateTime startDate, LocalDateTime endDate) {

        if (startDate.isAfter(endDate)) {
            throw new BadRequestException("Start date must be before end date");
        }

        List<Availability> availabilities = availabilityRepository.findAvailabilitiesForServiceBetweenDates(
                serviceId, startDate, endDate);

        return availabilities.stream()
                .map(availability -> modelMapper.map(availability, AvailabilityResponse.class))
                .collect(Collectors.toList());
    }

    public List<AvailabilityResponse> getAvailableDatesByServiceId(
            Long serviceId, LocalDateTime startDate, LocalDateTime endDate) {

        if (startDate.isAfter(endDate)) {
            throw new BadRequestException("Start date must be before end date");
        }

        List<Availability> availabilities = availabilityRepository.findAvailableSlotsForServiceBetweenDates(
                serviceId, startDate, endDate);

        return availabilities.stream()
                .map(availability -> modelMapper.map(availability, AvailabilityResponse.class))
                .collect(Collectors.toList());
    }

    @Transactional
    public AvailabilityResponse createAvailability(AvailabilityRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        Service service = serviceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Service not found with id: " + request.getServiceId()));

        // Verify the current user is the service owner
        if (!service.getUser().getId().equals(currentUser.getId()) &&
                !currentUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            throw new UnauthorizedException("You can only manage availabilities for your own services");
        }

        // Validate dates
        if (request.getStartDateTime().isAfter(request.getEndDateTime())) {
            throw new BadRequestException("Start date must be before end date");
        }

        Availability availability = Availability.builder()
                .service(service)
                .startDateTime(request.getStartDateTime())
                .endDateTime(request.getEndDateTime())
                .isAvailable(request.isAvailable())
                .notes(request.getNotes())
                .build();

        Availability savedAvailability = availabilityRepository.save(availability);
        return modelMapper.map(savedAvailability, AvailabilityResponse.class);
    }

    @Transactional
    public List<AvailabilityResponse> createBulkAvailabilities(List<AvailabilityRequest> requests) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        if (requests.isEmpty()) {
            throw new BadRequestException("At least one availability request is required");
        }

        // Verify all requests are for the same service
        Long serviceId = requests.get(0).getServiceId();
        boolean allSameService = requests.stream()
                .allMatch(req -> req.getServiceId().equals(serviceId));

        if (!allSameService) {
            throw new BadRequestException("All availability requests must be for the same service");
        }

        Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found with id: " + serviceId));

        // Verify the current user is the service owner
        if (!service.getUser().getId().equals(currentUser.getId()) &&
                !currentUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            throw new UnauthorizedException("You can only manage availabilities for your own services");
        }

        List<Availability> availabilities = requests.stream()
                .map(req -> {
                    // Validate dates
                    if (req.getStartDateTime().isAfter(req.getEndDateTime())) {
                        throw new BadRequestException("Start date must be before end date");
                    }

                    return Availability.builder()
                            .service(service)
                            .startDateTime(req.getStartDateTime())
                            .endDateTime(req.getEndDateTime())
                            .isAvailable(req.isAvailable())
                            .notes(req.getNotes())
                            .build();
                })
                .collect(Collectors.toList());

        List<Availability> savedAvailabilities = availabilityRepository.saveAll(availabilities);

        return savedAvailabilities.stream()
                .map(availability -> modelMapper.map(availability, AvailabilityResponse.class))
                .collect(Collectors.toList());
    }

    @Transactional
    public AvailabilityResponse updateAvailability(Long id, AvailabilityRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        Availability availability = availabilityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Availability not found with id: " + id));

        // Verify the current user is the service owner
        if (!availability.getService().getUser().getId().equals(currentUser.getId()) &&
                !currentUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            throw new UnauthorizedException("You can only update availabilities for your own services");
        }

        // Validate dates
        if (request.getStartDateTime() != null && request.getEndDateTime() != null &&
                request.getStartDateTime().isAfter(request.getEndDateTime())) {
            throw new BadRequestException("Start date must be before end date");
        }

        if (request.getStartDateTime() != null) {
            availability.setStartDateTime(request.getStartDateTime());
        }

        if (request.getEndDateTime() != null) {
            availability.setEndDateTime(request.getEndDateTime());
        }

        availability.setAvailable(request.isAvailable());

        if (request.getNotes() != null) {
            availability.setNotes(request.getNotes());
        }

        Availability updatedAvailability = availabilityRepository.save(availability);
        return modelMapper.map(updatedAvailability, AvailabilityResponse.class);
    }

    @Transactional
    public void deleteAvailability(Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        Availability availability = availabilityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Availability not found with id: " + id));

        // Verify the current user is the service owner
        if (!availability.getService().getUser().getId().equals(currentUser.getId()) &&
                !currentUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            throw new UnauthorizedException("You can only delete availabilities for your own services");
        }

        availabilityRepository.delete(availability);
    }
}