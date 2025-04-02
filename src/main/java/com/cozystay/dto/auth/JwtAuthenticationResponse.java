package com.cozystay.dto.auth;

import com.cozystay.model.Role;
import com.cozystay.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JwtAuthenticationResponse {

    private String token;
//    private User user;
    private Long userId;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private Set<Role> roles;
    private boolean isProvider;
}