package com.cozystay.repository;

import com.cozystay.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    Page<Review> findByServiceIdAndIsVisibleTrue(Long serviceId, Pageable pageable);

    Page<Review> findByUserId(Long userId, Pageable pageable);

    @Query("SELECT r FROM Review r WHERE r.service.user.id = :providerId")
    Page<Review> findByProviderId(@Param("providerId") Long providerId, Pageable pageable);

    Optional<Review> findByBookingId(Long bookingId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.service.id = :serviceId AND r.isVisible = true")
    Double calculateAverageRatingForService(@Param("serviceId") Long serviceId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.service.id = :serviceId AND r.isVisible = true")
    Integer countReviewsForService(@Param("serviceId") Long serviceId);
}