package com.cozystay.dto.booking;

import com.cozystay.dto.payment.PaymentResponse;
import com.cozystay.dto.review.ReviewResponse;
import com.cozystay.dto.service.ServiceDetailResponse;
import com.cozystay.dto.user.UserResponse;
import com.cozystay.model.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingDetailResponse {

    private Long id;
    private UserResponse user;
    private ServiceDetailResponse service;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private BigDecimal totalPrice;
    private Integer guestCount;
    private BookingStatus status;
    private String specialRequests;
    private String cancellationReason;
    private LocalDateTime cancelledAt;
    private PaymentResponse payment;
    private ReviewResponse review;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}