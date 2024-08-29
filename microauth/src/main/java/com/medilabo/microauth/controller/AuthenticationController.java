package com.medilabo.AuthenticationService.controller;

import com.medilabo.AuthenticationService.entity.UserCredentials;
import com.medilabo.AuthenticationService.service.AuthenticationService;
import dto.AuthRequestDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @PostMapping("/register")
    public void addNewUser(@RequestBody UserCredentials user) {
        authenticationService.saveUser(user);
    }


    //TODO add custom userNotFoundException (extends RunTimeException)
    @PostMapping("/token")
    public String getToken(@RequestBody AuthRequestDto authRequestDto) {
       Authentication userAuthentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authRequestDto.getUsername(),
                        authRequestDto.getPassword()));
        if(userAuthentication.isAuthenticated()) {
            System.out.println("User found in DB, generating token");
            return authenticationService.generateToken(authRequestDto.getUsername());

        } else {
            throw new RuntimeException("Invalid user credentials unable to get token");
        }
    }

    @GetMapping("/validate")
    public void validateToken(@RequestParam("token") String token) {
        authenticationService.validateToken(token);
    }
}
