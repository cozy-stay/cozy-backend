package com.cozystay.dto.review;

import com.cozystay.dto.service.ServiceResponse;
import com.cozystay.dto.user.UserResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewResponse {

    private Long id;
    private UserResponse user;
    private ServiceResponse service;
    private Long bookingId;
    private Integer rating;
    private String comment;
    private List<String> images;
    private boolean isVisible;
    private String ownerReply;
    private LocalDateTime ownerRepliedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}