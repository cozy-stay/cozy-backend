package com.cozystay.dto.booking;

import com.cozystay.dto.service.ServiceResponse;
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
public class BookingResponse {

    private Long id;
    private UserResponse user;
    private ServiceResponse service;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private BigDecimal totalPrice;
    private Integer guestCount;
    private BookingStatus status;
    private LocalDateTime createdAt;
}