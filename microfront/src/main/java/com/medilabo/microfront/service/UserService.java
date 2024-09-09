package com.medilabo.microfront.service;

import com.medilabo.microfront.beans.AuthRequestDto;
import com.medilabo.microfront.exception.WrongCredentialsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class UserService {
    @Autowired
    private WebClient.Builder webClientBuilder;

    public String getToken(String username, String password) {

        AuthRequestDto authRequestDto = new AuthRequestDto(username, password);

        return webClientBuilder.build()
                .post()
                .uri("http://microauth:8085/auth/token")
                .bodyValue(authRequestDto)
                .retrieve()
                .onStatus(
                        HttpStatusCode::is4xxClientError, clientResponse -> Mono.error(new WrongCredentialsException("Wrong credentials, unable to get token"))
                )
                .bodyToMono(String.class)
                .block();
    }


}
