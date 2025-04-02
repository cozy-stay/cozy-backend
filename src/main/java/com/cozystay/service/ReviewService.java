package com.cozystay.service;

import com.cozystay.dto.review.ReviewRequest;
import com.cozystay.dto.review.ReviewResponse;
import com.cozystay.dto.review.ReviewUpdateRequest;
import com.cozystay.exception.BadRequestException;
import com.cozystay.exception.ResourceNotFoundException;
import com.cozystay.exception.UnauthorizedException;
import com.cozystay.model.*;
import com.cozystay.model.Service;
import com.cozystay.repository.BookingRepository;
import com.cozystay.repository.ReviewRepository;
import com.cozystay.repository.ServiceRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

@org.springframework.stereotype.Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private ModelMapper modelMapper;

    public Page<ReviewResponse> getReviewsByServiceId(Long serviceId, Pageable pageable) {
        // Verify service exists
        if (!serviceRepository.existsById(serviceId)) {
            throw new ResourceNotFoundException("Service not found with id: " + serviceId);
        }

        Page<Review> reviews = reviewRepository.findByServiceIdAndIsVisibleTrue(serviceId, pageable);
        return reviews.map(review -> modelMapper.map(review, ReviewResponse.class));
    }

    public Page<ReviewResponse> getReviewsByUser(Pageable pageable) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        Page<Review> reviews = reviewRepository.findByUserId(currentUser.getId(), pageable);
        return reviews.map(review -> modelMapper.map(review, ReviewResponse.class));
    }

    public Page<ReviewResponse> getReviewsForProvider(Pageable pageable) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        // Verify user is a provider
//        if (!currentUser.getAuthorities().stream().anyMatch(a ->
//                a.getAuthority().equals("ROLE_PROVIDER") || a.getAuthority().equals("ROLE_ADMIN"))) {
//            throw new UnauthorizedException("You can only delete your own reviews");
//        }

//        Long serviceId = review.getService().getId();
//
//        reviewRepository.delete(review);

        // Update service average rating and review count
//        updateServiceRatingAndCount(serviceId);

        Page<Review> reviews = reviewRepository.findByProviderId(currentUser.getId(), pageable);
        return reviews.map(review -> modelMapper.map(review, ReviewResponse.class));
    }


    private void updateServiceRatingAndCount(Long serviceId) {
        Double avgRating = reviewRepository.calculateAverageRatingForService(serviceId);
        Integer reviewCount = reviewRepository.countReviewsForService(serviceId);

        Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found with id: " + serviceId));

        service.setAvgRating(avgRating != null ? avgRating : 0.0);
        service.setReviewCount(reviewCount != null ? reviewCount : 0);

        serviceRepository.save(service);
    }


    public ReviewResponse getReviewById(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + id));
        return modelMapper.map(review, ReviewResponse.class);
    }

    @Transactional
    public ReviewResponse createReview(ReviewRequest reviewRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        Booking booking = bookingRepository.findById(reviewRequest.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + reviewRequest.getBookingId()));

        // Verify the booking belongs to the current user
        if (!booking.getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You can only review your own bookings");
        }

        // Verify the booking is completed
        if (booking.getStatus() != BookingStatus.COMPLETED) {
            throw new BadRequestException("You can only review completed bookings");
        }

        // Check if a review already exists for this booking
        Optional<Review> existingReview = reviewRepository.findByBookingId(booking.getId());
        if (existingReview.isPresent()) {
            throw new BadRequestException("A review already exists for this booking");
        }

        Service service = booking.getService();

        Review review = Review.builder()
                .user(currentUser)
                .service(service)
                .booking(booking)
                .rating(reviewRequest.getRating())
                .comment(reviewRequest.getComment())
                .images(reviewRequest.getImages() != null ? new ArrayList<>(reviewRequest.getImages()) : new ArrayList<>())
                .isVisible(true)
                .build();

        Review savedReview = reviewRepository.save(review);

        // Update service average rating and review count
        updateServiceRatingAndCount(service.getId());

        return modelMapper.map(savedReview, ReviewResponse.class);
    }

    @Transactional
    public ReviewResponse updateReview(Long id, ReviewUpdateRequest updateRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + id));

        // Verify the review belongs to the current user
        if (!review.getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You can only update your own reviews");
        }

        if (updateRequest.getRating() != null) {
            review.setRating(updateRequest.getRating());
        }

        if (updateRequest.getComment() != null) {
            review.setComment(updateRequest.getComment());
        }

        if (updateRequest.getImages() != null) {
            review.setImages(new ArrayList<>(updateRequest.getImages()));
        }

        Review updatedReview = reviewRepository.save(review);

        // Update service average rating
        updateServiceRatingAndCount(review.getService().getId());

        return modelMapper.map(updatedReview, ReviewResponse.class);
    }

    @Transactional
    public ReviewResponse addOwnerReply(Long id, String reply) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + id));

        // Verify the current user is the service owner
        if (!review.getService().getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("Only the service owner can reply to reviews");
        }

        review.setOwnerReply(reply);
        review.setOwnerRepliedAt(LocalDateTime.now());

        Review updatedReview = reviewRepository.save(review);
        return modelMapper.map(updatedReview, ReviewResponse.class);
    }

    @Transactional
    public ReviewResponse toggleReviewVisibility(Long id, boolean isVisible) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + id));

        // Only admins can toggle review visibility
        if (!currentUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            throw new UnauthorizedException("You don't have permission to toggle review visibility");
        }

        review.setVisible(isVisible);
        Review updatedReview = reviewRepository.save(review);

        // Update service average rating and review count
        updateServiceRatingAndCount(review.getService().getId());

        return modelMapper.map(updatedReview, ReviewResponse.class);
    }
}

//@Transactional
//public void deleteReview(Long id) {
//    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//    User currentUser = (User) authentication.getPrincipal();
//
//    Review review = reviewRepository.findById(id)
//            .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + id));
//
//    // Verify the review belongs to the current user or user is admin
//    boolean isAdmin = currentUser.getAuthorities().stream()
//            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
//
//    if (!review.getUser().getId().equals(currentUser.getId()) && !isAdmin) {
//        throw new UnauthorizedException("