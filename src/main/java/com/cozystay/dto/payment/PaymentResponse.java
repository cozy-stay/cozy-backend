package com.cozystay.dto.payment;

import com.cozystay.model.PaymentMethod;
import com.cozystay.model.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentResponse {

    private Long id;
    private String transactionId;
    private BigDecimal amount;
    private PaymentStatus status;
    private PaymentMethod method;
    private String notes;
    private LocalDateTime paidAt;
    private LocalDateTime refundedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}