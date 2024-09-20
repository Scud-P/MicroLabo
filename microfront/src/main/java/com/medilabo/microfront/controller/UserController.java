package com.medilabo.microfront.controller;

import com.medilabo.microfront.exception.WrongCredentialsException;
import com.medilabo.microfront.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
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

    @Operation(summary = "Displays the login page",
            description = "Displays the HTML page where the registered users can log in.",
            parameters = {
                    @Parameter(
                            name = "error",
                            description = "An error added to params in case of redirect after trying to log in with bad credentials",
                            required = false,
                            schema = @Schema(type = "string"))},
            responses = {
                    @ApiResponse(
                            description = "HTML page displaying the login form",
                            content = @Content(
                                    mediaType = "text/html"
                            )),
                    @ApiResponse(
                            description = "HTML page displaying the login form with a Wrong credentials message added",
                            content = @Content(
                                    mediaType = "text/html"))
            })
    @GetMapping("/api/login")
    public String showLoginPage(@RequestParam(value = "error", required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("errorMessage", error);
        }
        return "login";
    }

    @Operation(summary = "Logs the user in",
            description = "Logs the user in with the provided username and password. On success, redirects to the home page. On failure, redirects back to the login page with an error message.",
            parameters = {
                    @Parameter(
                            name = "username",
                            description = "The username of the user attempting to log in.",
                            required = true,
                            schema = @Schema(type = "string")),
                    @Parameter(
                            name = "password",
                            description = "The password of the user attempting to log in.",
                            required = true,
                            schema = @Schema(type = "string"))},
            responses = {
                    @ApiResponse(
                            responseCode = "302",
                            description = "Login successful. Redirects to the home page.",
                            content = @Content(mediaType = "text/html"),
                            headers = {
                                    @Header(
                                            name = "Set-Cookie",
                                            description = "Contains the 'token' cookie used for subsequent authentication.",
                                            schema = @Schema(type = "string")),
                                    @Header(
                                            name = "Location",
                                            description = "URL of the redirected home page.",
                                            schema = @Schema(type = "string"))
                            }),
                    @ApiResponse(
                            responseCode = "302",
                            description = "Login failed. Redirects back to the login page with an error message.",
                            content = @Content(mediaType = "text/html"),
                            headers = {
                                    @Header(
                                            name = "Location",
                                            description = "URL of the login page with an appended error message.",
                                            schema = @Schema(type = "string"))
                            })
            })
    @PostMapping("/api/login")
    public ResponseEntity<String> validateLogin(@RequestParam("username") String username,
                                                @RequestParam("password") String password) {
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


    @Operation(
            summary = "Logs the user out",
            description = "Invalidates the JWT token stored in the cookie and redirects the user to the login page with a success message.",
            responses = {
                    @ApiResponse(
                            responseCode = "302",
                            description = "Logout successful. Redirects to the login page with a success message.",
                            content = @Content(mediaType = "text/html"),
                            headers = {
                                    @Header(
                                            name = "Set-Cookie",
                                            description = "Removes the 'token' cookie by setting its expiration time to 0.",
                                            schema = @Schema(type = "string")
                                    ),
                                    @Header(
                                            name = "Location",
                                            description = "URL of the login page with the success message appended.",
                                            schema = @Schema(type = "string")
                                    )
                            }
                    )
            }
    )
    @PostMapping("/api/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        Cookie invalidatedCookie = new Cookie("token", null);
        invalidatedCookie.setHttpOnly(true);
        invalidatedCookie.setSecure(false);
        invalidatedCookie.setPath("/");
        invalidatedCookie.setMaxAge(0);
        response.addCookie(invalidatedCookie);

        System.out.println(invalidatedCookie);

        String redirectUrl = "/api/logout-success";
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(redirectUrl));
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    @GetMapping("/api/logout-success")
    public String getLogoutSuccess() {
        return "logout-success";
    }

    @GetMapping("/api/logout")
    public String getLogout() {
        return "logout" ;
    }

}
