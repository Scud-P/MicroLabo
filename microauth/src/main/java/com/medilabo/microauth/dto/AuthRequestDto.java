package com.medilabo.microauth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) for authentication requests.
 * This class encapsulates the username and password required for user authentication.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthRequestDto {
    private String username;
    private String password;
}
