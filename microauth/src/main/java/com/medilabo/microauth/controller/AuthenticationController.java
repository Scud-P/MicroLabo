package com.medilabo.microauth.controller;

import com.medilabo.microauth.dto.AuthRequestDto;
import com.medilabo.microauth.entity.UserCredentials;
import com.medilabo.microauth.exception.WrongCredentialsException;
import com.medilabo.microauth.service.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * The {@code AuthenticationController} class handles authentication-related HTTP requests
 * for user registration and token generation.
 * This controller provides endpoints for user registration, generating JWT tokens,
 * and validating tokens. It utilizes the {@link AuthenticationService} for business logic
 * related to user authentication and token management.
 */
@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private AuthenticationManager authenticationManager;

    /**
     * Registers a new user by saving their credentials.
     * This method accepts a {@link UserCredentials} object in the request body and
     * invokes the {@link AuthenticationService} to save the user.
     *
     * @param user the {@link UserCredentials} object containing user information
     */
    @PostMapping("/register")
    public void addNewUser(@RequestBody UserCredentials user) {
        authenticationService.saveUser(user);
    }

    /**
     * Generates a JWT token for the user based on the provided credentials.
     * This method authenticates the user using the provided {@link AuthRequestDto}
     * object containing the username and password. If authentication is successful,
     * a JWT token is generated and returned. Otherwise, a {@link WrongCredentialsException}
     * is thrown.
     *
     * @param authRequestDto the {@link AuthRequestDto} containing the user's credentials
     * @return the generated JWT token
     * @throws WrongCredentialsException if the user credentials are invalid
     */
    @PostMapping("/token")
    public String getToken(@RequestBody AuthRequestDto authRequestDto) {
        Authentication userAuthentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authRequestDto.getUsername(),
                        authRequestDto.getPassword()));
        if (userAuthentication.isAuthenticated()) {
            return authenticationService.generateToken(authRequestDto.getUsername());

        } else {
            throw new WrongCredentialsException("Invalid user credentials, unable to get token");
        }
    }
    /**
     * Validates the provided JWT token.
     * This method checks the validity of the token passed in the request header.
     * If valid, it confirms the validation; otherwise, an exception may be thrown.
     *
     * @param token the JWT token to validate
     * @return a confirmation message if the token is valid
     */
    @GetMapping("/validate")
    public String validateToken(@RequestHeader("token") String token) {
        authenticationService.validateToken(token);
        return "Token has been validated by authentication service";
    }
}
