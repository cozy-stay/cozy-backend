package com.cozystay.repository;

import com.cozystay.model.Booking;
import com.cozystay.model.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    Page<Booking> findByUserId(Long userId, Pageable pageable);

    @Query("SELECT b FROM Booking b WHERE b.service.user.id = :providerId")
    Page<Booking> findByProviderId(@Param("providerId") Long providerId, Pageable pageable);

    Page<Booking> findByServiceId(Long serviceId, Pageable pageable);

    Page<Booking> findByUserIdAndStatus(Long userId, BookingStatus status, Pageable pageable);

    @Query("SELECT b FROM Booking b WHERE b.service.user.id = :providerId AND b.status = :status")
    Page<Booking> findByProviderIdAndStatus(@Param("providerId") Long providerId,
                                            @Param("status") BookingStatus status,
                                            Pageable pageable);

    @Query("SELECT b FROM Booking b WHERE b.startDateTime BETWEEN :startDate AND :endDate")
    List<Booking> findBookingsBetweenDates(@Param("startDate") LocalDateTime startDate,
                                           @Param("endDate") LocalDateTime endDate);

    @Query("SELECT b FROM Booking b WHERE b.service.id = :serviceId AND " +
            "b.status NOT IN(CANCELLED_BY_USER, CANCELLED_BY_PROVIDER) OR " +
            "((b.startDateTime BETWEEN :startDate AND :endDate) OR " +
            "(b.endDateTime BETWEEN :startDate AND :endDate) OR " +
            "(b.startDateTime <= :startDate AND b.endDateTime >= :endDate))")
    List<Booking> findOverlappingBookings(@Param("serviceId") Long serviceId,
                                          @Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate);
}