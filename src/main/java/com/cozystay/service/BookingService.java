package com.cozystay.service;

import com.cozystay.dto.booking.BookingDetailResponse;
import com.cozystay.dto.booking.BookingRequest;
import com.cozystay.dto.booking.BookingResponse;
import com.cozystay.exception.BadRequestException;
import com.cozystay.exception.ResourceNotFoundException;
import com.cozystay.exception.UnauthorizedException;
import com.cozystay.model.*;
import com.cozystay.repository.AvailabilityRepository;
import com.cozystay.repository.BookingRepository;
import com.cozystay.repository.ServiceRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private AvailabilityRepository availabilityRepository;

    @Autowired
    private ModelMapper modelMapper;

    public Page<BookingResponse> getCurrentUserBookings(Pageable pageable) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        Page<Booking> bookings = bookingRepository.findByUserId(currentUser.getId(), pageable);
        return bookings.map(booking -> modelMapper.map(booking, BookingResponse.class));
    }

    public Page<BookingResponse> getBookingsForProvider(Pageable pageable) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        // Verify user is a provider
        if (!currentUser.getAuthorities().stream().anyMatch(a ->
                a.getAuthority().equals("ROLE_PROVIDER") || a.getAuthority().equals("ROLE_ADMIN"))) {
            throw new UnauthorizedException("You don't have permission to view provider bookings");
        }

        Page<Booking> bookings = bookingRepository.findByProviderId(currentUser.getId(), pageable);
        return bookings.map(booking -> modelMapper.map(booking, BookingResponse.class));
    }

    public BookingDetailResponse getBookingById(Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));

        // Verify user is authorized to view this booking
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isBookingUser = booking.getUser().getId().equals(currentUser.getId());
        boolean isServiceProvider = booking.getService().getUser().getId().equals(currentUser.getId());

        if (!isAdmin && !isBookingUser && !isServiceProvider) {
            throw new UnauthorizedException("You don't have permission to view this booking");
        }

        return modelMapper.map(booking, BookingDetailResponse.class);
    }

    @Transactional
    public BookingDetailResponse updateBookingStatus(Long id, BookingStatus status, String cancellationReason) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));

        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isBookingUser = booking.getUser().getId().equals(currentUser.getId());
        boolean isServiceProvider = booking.getService().getUser().getId().equals(currentUser.getId());

        // Validate permissions based on the requested status change
        switch (status) {
            case CONFIRMED:
                // Only provider or admin can confirm booking
                if (!isAdmin && !isServiceProvider) {
                    throw new UnauthorizedException("Only service providers can confirm bookings");
                }
                break;

            case CANCELLED_BY_USER:
                // Only user or admin can cancel as user
                if (!isAdmin && !isBookingUser) {
                    throw new UnauthorizedException("Only booking users can cancel as user");
                }

                if (cancellationReason == null || cancellationReason.trim().isEmpty()) {
                    throw new BadRequestException("Cancellation reason is required");
                }

                booking.setCancellationReason(cancellationReason);
                booking.setCancelledAt(LocalDateTime.now());
                break;

            case CANCELLED_BY_PROVIDER:
                // Only provider or admin can cancel as provider
                if (!isAdmin && !isServiceProvider) {
                    throw new UnauthorizedException("Only service providers can cancel as provider");
                }

                if (cancellationReason == null || cancellationReason.trim().isEmpty()) {
                    throw new BadRequestException("Cancellation reason is required");
                }

                booking.setCancellationReason(cancellationReason);
                booking.setCancelledAt(LocalDateTime.now());
                break;

            case COMPLETED:
                // Only provider or admin can mark as completed
                if (!isAdmin && !isServiceProvider) {
                    throw new UnauthorizedException("Only service providers can mark bookings as completed");
                }

                // Verify that the end date has passed
                if (booking.getEndDateTime().isAfter(LocalDateTime.now())) {
                    throw new BadRequestException("Cannot mark booking as completed before its end date");
                }
                break;

            case NO_SHOW:
                // Only provider or admin can mark as no-show
                if (!isAdmin && !isServiceProvider) {
                    throw new UnauthorizedException("Only service providers can mark bookings as no-show");
                }

                // Verify that the start date has passed
                if (booking.getStartDateTime().isAfter(LocalDateTime.now())) {
                    throw new BadRequestException("Cannot mark booking as no-show before its start date");
                }
                break;

            default:
                throw new BadRequestException("Invalid booking status");
        }

        booking.setStatus(status);
        Booking updatedBooking = bookingRepository.save(booking);
        return modelMapper.map(updatedBooking, BookingDetailResponse.class);
    }

    public Page<BookingResponse> filterBookingsByStatus(BookingStatus status, Pageable pageable) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        Page<Booking> bookings = bookingRepository.findByUserIdAndStatus(currentUser.getId(), status, pageable);
        return bookings.map(booking -> modelMapper.map(booking, BookingResponse.class));
    }

    public Page<BookingResponse> getProviderBookingsByStatus(BookingStatus status, Pageable pageable) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        // Verify user is a provider
        if (!currentUser.getAuthorities().stream().anyMatch(a ->
                a.getAuthority().equals("ROLE_PROVIDER") || a.getAuthority().equals("ROLE_ADMIN"))) {
            throw new UnauthorizedException("You don't have permission to view provider bookings");
        }

        Page<Booking> bookings = bookingRepository.findByProviderIdAndStatus(currentUser.getId(), status, pageable);
        return bookings.map(booking -> modelMapper.map(booking, BookingResponse.class));
    }

    public BookingDetailResponse createBooking(BookingRequest bookingRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        com.cozystay.model.Service service = serviceRepository.findById(bookingRequest.getServiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Service not found with id: " + bookingRequest.getServiceId()));

        // Check if service is active
        if (!service.isActive()) {
            throw new BadRequestException("Service is not available for booking");
        }

        // Validate dates
        if (bookingRequest.getStartDateTime().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Booking start date cannot be in the past");
        }

        if (bookingRequest.getEndDateTime().isBefore(bookingRequest.getStartDateTime())) {
            throw new BadRequestException("Booking end date must be after start date");
        }

        // Check if the service is available for the requested dates
        List<Availability> availabilities = availabilityRepository.findAvailableSlotsForServiceBetweenDates(
                service.getId(), bookingRequest.getStartDateTime(), bookingRequest.getEndDateTime());

        if (availabilities.isEmpty()) {
            throw new BadRequestException("Service is not available for the requested dates");
        }

        // Check for overlapping bookings
        List<Booking> overlappingBookings = bookingRepository.findOverlappingBookings(
                service.getId(), bookingRequest.getStartDateTime(), bookingRequest.getEndDateTime());

        if (!overlappingBookings.isEmpty()) {
            throw new BadRequestException("Service is already booked for the requested dates");
        }

        // Calculate total price based on service pricing and duration
        BigDecimal totalPrice;
        Duration duration;

        switch (service.getPricingUnit()) {
            case PER_NIGHT:
                // Calculate nights (day differences)
                duration = Duration.between(
                        bookingRequest.getStartDateTime().toLocalDate().atStartOfDay(),
                        bookingRequest.getEndDateTime().toLocalDate().atStartOfDay());
                long nights = duration.toDays();
                totalPrice = service.getPrice().multiply(BigDecimal.valueOf(nights));
                break;

            case PER_DAY:
                // Calculate days (including partial days)
                duration = Duration.between(bookingRequest.getStartDateTime(), bookingRequest.getEndDateTime());
                long hours = duration.toHours();
                double days = Math.ceil(hours / 24.0);
                totalPrice = service.getPrice().multiply(BigDecimal.valueOf(days));
                break;

            case PER_HOUR:
                // Calculate hours (including partial hours)
                duration = Duration.between(bookingRequest.getStartDateTime(), bookingRequest.getEndDateTime());
                long minutes = duration.toMinutes();
                double hoursDouble = Math.ceil(minutes / 60.0);
                totalPrice = service.getPrice().multiply(BigDecimal.valueOf(hoursDouble));
                break;

            case PER_PERSON:
                // Per person pricing
                if (bookingRequest.getGuestCount() == null || bookingRequest.getGuestCount() <= 0) {
                    throw new BadRequestException("Guest count is required for per-person pricing");
                }
                totalPrice = service.getPrice().multiply(BigDecimal.valueOf(bookingRequest.getGuestCount()));
                break;

            case FIXED_PRICE:
            default:
                // Fixed price regardless of duration
                totalPrice = service.getPrice();
                break;
        }

        // Create booking
        Booking booking = Booking.builder()
                .user(currentUser)
                .service(service)
                .startDateTime(bookingRequest.getStartDateTime())
                .endDateTime(bookingRequest.getEndDateTime())
                .totalPrice(totalPrice)
                .guestCount(bookingRequest.getGuestCount())
                .status(BookingStatus.PENDING)
                .specialRequests(bookingRequest.getSpecialRequests())
                .build();

        Booking savedBooking = bookingRepository.save(booking);
        return modelMapper.map(savedBooking, BookingDetailResponse.class);
    }
}