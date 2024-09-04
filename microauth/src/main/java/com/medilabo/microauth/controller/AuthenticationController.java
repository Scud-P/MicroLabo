package com.medilabo.microauth.controller;

import com.medilabo.microauth.dto.AuthRequestDto;
import com.medilabo.microauth.entity.UserCredentials;
import com.medilabo.microauth.service.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
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
// TODO SWITCH TO POST IN CASE IT FUCKS
    @GetMapping("/validate")
    public String validateToken(@RequestHeader("token") String token) {
        authenticationService.validateToken(token);
        return "Token " + token + " has been validated by authentication module";
    }
}
