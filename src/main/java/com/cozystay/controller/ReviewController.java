package com.cozystay.controller;

import com.cozystay.dto.review.ReviewRequest;
import com.cozystay.dto.review.ReviewResponse;
import com.cozystay.dto.review.ReviewUpdateRequest;
import com.cozystay.service.ReviewService;
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
@RequestMapping("/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @GetMapping("/service/{serviceId}")
    public ResponseEntity<Page<ReviewResponse>> getReviewsByServiceId(
            @PathVariable Long serviceId,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<ReviewResponse> reviews = reviewService.getReviewsByServiceId(serviceId, pageable);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/user")
    public ResponseEntity<Page<ReviewResponse>> getReviewsByUser(
            @PageableDefault(size = 10) Pageable pageable) {
        Page<ReviewResponse> reviews = reviewService.getReviewsByUser(pageable);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/provider")
    @PreAuthorize("hasRole('PROVIDER') or hasRole('ADMIN')")
    public ResponseEntity<Page<ReviewResponse>> getReviewsForProvider(
            @PageableDefault(size = 10) Pageable pageable) {
        Page<ReviewResponse> reviews = reviewService.getReviewsForProvider(pageable);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReviewResponse> getReviewById(@PathVariable Long id) {
        ReviewResponse review = reviewService.getReviewById(id);
        return ResponseEntity.ok(review);
    }

    @PostMapping
    public ResponseEntity<ReviewResponse> createReview(
            @Valid @RequestBody ReviewRequest reviewRequest) {
        ReviewResponse review = reviewService.createReview(reviewRequest);
        return new ResponseEntity<>(review, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReviewResponse> updateReview(
            @PathVariable Long id,
            @Valid @RequestBody ReviewUpdateRequest updateRequest) {
        ReviewResponse review = reviewService.updateReview(id, updateRequest);
        return ResponseEntity.ok(review);
    }

    @PostMapping("/{id}/reply")
    @PreAuthorize("hasRole('PROVIDER') or hasRole('ADMIN')")
    public ResponseEntity<ReviewResponse> addOwnerReply(
            @PathVariable Long id,
            @RequestParam String reply) {
        ReviewResponse review = reviewService.addOwnerReply(id, reply);
        return ResponseEntity.ok(review);
    }

    @PatchMapping("/{id}/visibility")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReviewResponse> toggleReviewVisibility(
            @PathVariable Long id,
            @RequestParam boolean isVisible) {
        ReviewResponse review = reviewService.toggleReviewVisibility(id, isVisible);
        return ResponseEntity.ok(review);
    }

//    @DeleteMapping("/{id}")
//    public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
//        reviewService.deleteReview(id);
//        return ResponseEntity.noContent().build();
//    }
}