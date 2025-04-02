package com.cozystay.repository;

import com.cozystay.model.Location;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {

    List<Location> findByIsActiveTrue();

    List<Location> findByIsPopularAndIsActiveTrue(boolean isPopular);

    Optional<Location> findByCityIgnoreCaseAndRegionIgnoreCaseAndCountryIgnoreCase(
            String city, String region, String country);

    @Query("SELECT l FROM Location l WHERE LOWER(l.city) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(l.region) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(l.country) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Location> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query(value = "SELECT l.* FROM locations l " +
            "JOIN services s ON l.id = s.location_id " +
            "WHERE l.is_active = true " +
            "GROUP BY l.id " +
            "ORDER BY COUNT(s.id) DESC",
            nativeQuery = true)
    List<Location> findMostPopularLocations(Pageable pageable);
}