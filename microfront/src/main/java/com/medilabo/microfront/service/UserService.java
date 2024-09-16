package com.medilabo.microfront.service;

import com.medilabo.microfront.beans.AuthRequestDto;
import com.medilabo.microfront.exception.WrongCredentialsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Service class responsible for managing user authentication and retrieving tokens using WebClient.
 * Provides methods to authenticate a user and fetch a token based on credentials.
 */
@Service
public class UserService {
    @Autowired
    private WebClient.Builder webClientBuilder;

    /**
     * Retrieves an authentication token for a user based on their username and password.
     *
     * @param username The username of the user.
     * @param password The password of the user.
     * @return a String representing the authentication token.
     * @throws WrongCredentialsException if the provided credentials are incorrect.
     */
    public String getToken(String username, String password) {

        AuthRequestDto authRequestDto = new AuthRequestDto(username, password);

        return webClientBuilder.build()
                .post()
                .uri("/auth/token")
                .bodyValue(authRequestDto)
                .retrieve()
                .onStatus(
                        HttpStatusCode::is4xxClientError, clientResponse -> Mono.error(new WrongCredentialsException("Wrong credentials, unable to get token"))
                )
                .bodyToMono(String.class)
                .block();
    }
}
