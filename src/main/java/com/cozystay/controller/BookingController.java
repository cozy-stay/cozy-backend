package com.cozystay.controller;

import com.cozystay.dto.booking.BookingDetailResponse;
import com.cozystay.dto.booking.BookingRequest;
import com.cozystay.dto.booking.BookingResponse;
import com.cozystay.model.BookingStatus;
import com.cozystay.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @GetMapping
    public ResponseEntity<Page<BookingResponse>> getCurrentUserBookings(
            @PageableDefault(size = 10) Pageable pageable) {
        Page<BookingResponse> bookings = bookingService.getCurrentUserBookings(pageable);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/provider")
    @PreAuthorize("hasRole('PROVIDER') or hasRole('ADMIN')")
    public ResponseEntity<Page<BookingResponse>> getBookingsForProvider(
            @PageableDefault(size = 10) Pageable pageable) {
        Page<BookingResponse> bookings = bookingService.getBookingsForProvider(pageable);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingDetailResponse> getBookingById(@PathVariable Long id) {
        BookingDetailResponse booking = bookingService.getBookingById(id);
        return ResponseEntity.ok(booking);
    }

    @PostMapping
    public ResponseEntity<BookingDetailResponse> createBooking(
            @Valid @RequestBody BookingRequest bookingRequest) {
        BookingDetailResponse booking = bookingService.createBooking(bookingRequest);
        return new ResponseEntity<>(booking, HttpStatus.CREATED);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<BookingDetailResponse> updateBookingStatus(
            @PathVariable Long id,
            @RequestParam BookingStatus status,
            @RequestParam(required = false) String cancellationReason) {
        BookingDetailResponse booking = bookingService.updateBookingStatus(id, status, cancellationReason);
        return ResponseEntity.ok(booking);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<Page<BookingResponse>> filterBookingsByStatus(
            @PathVariable BookingStatus status,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<BookingResponse> bookings = bookingService.filterBookingsByStatus(status, pageable);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/provider/status/{status}")
    @PreAuthorize("hasRole('PROVIDER') or hasRole('ADMIN')")
    public ResponseEntity<Page<BookingResponse>> getProviderBookingsByStatus(
            @PathVariable BookingStatus status,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<BookingResponse> bookings = bookingService.getProviderBookingsByStatus(status, pageable);
        return ResponseEntity.ok(bookings);
    }
}