package com.cozystay.repository;

import com.cozystay.model.Availability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AvailabilityRepository extends JpaRepository<Availability, Long> {

    List<Availability> findByServiceId(Long serviceId);

    @Query("SELECT a FROM Availability a WHERE a.service.id = :serviceId AND " +
            "a.startDateTime <= :endDate AND a.endDateTime >= :startDate")
    List<Availability> findAvailabilitiesForServiceBetweenDates(
            @Param("serviceId") Long serviceId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT a FROM Availability a WHERE a.service.id = :serviceId AND " +
            "a.isAvailable = true AND " +
            "a.startDateTime <= :endDate AND a.endDateTime >= :startDate")
    List<Availability> findAvailableSlotsForServiceBetweenDates(
            @Param("serviceId") Long serviceId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}