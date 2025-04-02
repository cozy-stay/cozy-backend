package com.cozystay.repository;

import com.cozystay.model.Service;
import com.cozystay.model.ServiceType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ServiceRepository extends JpaRepository<Service, Long>, JpaSpecificationExecutor<Service> {

    Page<Service> findByIsActiveTrue(Pageable pageable);

    Page<Service> findByTypeAndIsActiveTrue(ServiceType type, Pageable pageable);

    Page<Service> findByCategoryIdAndIsActiveTrue(Long categoryId, Pageable pageable);

    Page<Service> findByLocationIdAndIsActiveTrue(Long locationId, Pageable pageable);

    @Query("SELECT s FROM Service s WHERE s.isActive = true AND " +
            "s.price BETWEEN :minPrice AND :maxPrice")
    Page<Service> findByPriceRange(@Param("minPrice") BigDecimal minPrice,
                                   @Param("maxPrice") BigDecimal maxPrice,
                                   Pageable pageable);

    @Query("SELECT s FROM Service s WHERE s.isActive = true AND " +
            "s.title LIKE %:keyword% OR s.description LIKE %:keyword%")
    Page<Service> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT s FROM Service s WHERE s.user.id = :userId")
    Page<Service> findByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT s FROM Service s WHERE s.isActive = true " +
            "ORDER BY s.avgRating DESC")
    Page<Service> findTopRatedServices(Pageable pageable);

    @Query(value = "SELECT s.* FROM services s " +
            "JOIN bookings b ON s.id = b.service_id " +
            "WHERE s.is_active = true " +
            "GROUP BY s.id " +
            "ORDER BY COUNT(b.id) DESC",
            nativeQuery = true)
    List<Service> findMostPopularServices(Pageable pageable);
}