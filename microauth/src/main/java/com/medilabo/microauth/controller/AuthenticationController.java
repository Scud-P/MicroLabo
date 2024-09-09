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

    @PostMapping("/token")
    public String getToken(@RequestBody AuthRequestDto authRequestDto) {
       Authentication userAuthentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authRequestDto.getUsername(),
                        authRequestDto.getPassword()));
        if(userAuthentication.isAuthenticated()) {
            return authenticationService.generateToken(authRequestDto.getUsername());

        } else {
            throw new WrongCredentialsException("Invalid user credentials, unable to get token");
        }
    }

    @GetMapping("/validate")
    public String validateToken(@RequestHeader("token") String token) {
        authenticationService.validateToken(token);
        return "Token has been validated by authentication service";
    }
}
