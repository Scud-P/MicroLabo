package com.medilabo.microauth.service;

import com.medilabo.microauth.entity.UserCredentials;
import com.medilabo.microauth.repository.UserCredentialsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * The {@code AuthenticationService} class provides methods for handling user authentication
 * operations, including user registration, token generation, and token validation.
 *
 * This service interacts with the {@link UserCredentialsRepository} to manage user data
 * and utilizes {@link PasswordEncoder} for securely hashing passwords.
 */
@Service
public class AuthenticationService {

    @Autowired
    private UserCredentialsRepository userCredentialsRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    /**
     * Saves a new user credential by encoding the password and storing it in the database.
     * This method receives a {@link UserCredentials} object, encodes the user's password
     * using the {@link PasswordEncoder}, and saves the credentials in the repository.
     *
     * @param credentials the {@link UserCredentials} object containing user information
     */
    public void saveUser(UserCredentials credentials) {
        credentials.setPassword(passwordEncoder.encode(credentials.getPassword()));
        userCredentialsRepository.save(credentials);
    }

    /**
     * Generates a JWT token for the specified username.
     * This method calls the {@link JwtService} to create a token for the provided username,
     * which can be used for authentication purposes.
     *
     * @param username the username for which the token will be generated
     * @return a JWT token as a {@link String}
     */
    public String generateToken(String username) {
        return jwtService.generateToken(username);
    }

    /**
     * Validates the provided JWT token.
     * This method utilizes the {@link JwtService} to check the validity of the given token.
     * If the token is invalid, an exception will be thrown.
     *
     * @param token the JWT token to validate
     */
    public void validateToken(String token) {
        jwtService.validateToken(token);
    }
}
