package com.medilabo.microfront.controller;

import com.medilabo.microfront.exception.WrongCredentialsException;
import com.medilabo.microfront.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriUtils;

import java.net.URI;
import java.nio.charset.StandardCharsets;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private WebClient.Builder webClientBuilder;

    @GetMapping("/api/login")
    public String showLoginPage(@RequestParam(value = "error", required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("errorMessage", error);
        }
        return "login";
    }

    @PostMapping("/api/login")
    public ResponseEntity<String> validateLogin(@RequestParam("username") String username,
                                              @RequestParam("password") String password,
                                              Model model) {
        try {
            String token = userService.getToken(username, password);

            ResponseCookie cookie = ResponseCookie.from("token", token)
                    .path("/")
                    .httpOnly(true)
                    .build();

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.SET_COOKIE, cookie.toString());

            headers.setLocation(URI.create("/api/home"));
            return new ResponseEntity<>(headers, HttpStatus.FOUND);

        } catch (WrongCredentialsException e) {
            String errorMessage = e.getMessage();
            String redirectUrl = "/api/login?error=" + UriUtils.encode(errorMessage, StandardCharsets.UTF_8);
            HttpHeaders headers = new HttpHeaders();
            headers.setLocation(URI.create(redirectUrl));
            return new ResponseEntity<>(headers, HttpStatus.FOUND);
        }
    }
}
