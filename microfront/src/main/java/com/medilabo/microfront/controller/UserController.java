package com.medilabo.microfront.controller;

import com.medilabo.microfront.beans.AuthRequestDto;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URI;

@Controller
public class UserController {

    @Autowired
    private WebClient.Builder webClientBuilder;

    @GetMapping("/api/login")
    public String login() {
        return "login";
    }

    // TODO handle Bad Credentials

    @PostMapping("/api/login")
    public ResponseEntity<Void> validateLogin(@RequestParam("username") String username,
                                              @RequestParam("password") String password) {

        AuthRequestDto authRequestDto = new AuthRequestDto(username, password);

        String token = webClientBuilder.build()
                .post()
                .uri("http://localhost:8085/auth/token")
                .bodyValue(authRequestDto)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        ResponseCookie cookie = ResponseCookie.from("token", token)
                .path("/")
                .httpOnly(true)
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, cookie.toString());

        // Redirect to the home page
        headers.setLocation(URI.create("/api/home"));
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

}
