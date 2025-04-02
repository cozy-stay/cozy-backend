package com.cozystay.dto.user;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserRequest {

    @Size(max = 50, message = "First name must be less than 50 characters")
    private String firstName;

    @Size(max = 50, message = "Last name must be less than 50 characters")
    private String lastName;

    @Size(max = 15, message = "Phone number must be less than 15 characters")
    private String phone;

    @Size(max = 255, message = "Bio must be less than 255 characters")
    private String bio;

    private String profileImage;

    @Size(min = 6, max = 120, message = "Password must be between 6 and 120 characters")
    private String password;
}