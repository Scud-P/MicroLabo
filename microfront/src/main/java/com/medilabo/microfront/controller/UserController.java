package com.medilabo.microfront.controller;

import com.medilabo.microfront.exception.WrongCredentialsException;
import com.medilabo.microfront.service.UserService;
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

/**
 * Controller for managing user authentication, including login and logout operations.
 */
@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private WebClient.Builder webClientBuilder;

    /**
     * Displays the login page for registered users.
     *
     * @param error Optional parameter that carries an error message if login fails.
     * @param model Model to add attributes to be displayed on the login page.
     * @return The name of the view to be rendered, which is the login page.
     */
    @GetMapping("/api/login")
    public String showLoginPage(@RequestParam(value = "error", required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("errorMessage", error);
        }
        return "login";
    }

    /**
     * Validates the user's login credentials and logs the user in if successful.
     *
     * @param username The username of the user attempting to log in.
     * @param password The password of the user attempting to log in.
     * @return A response entity containing headers for redirection, including a token cookie if login is successful.
     */
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


    /**
     * Logs the user out by invalidating the JWT token stored in the cookie and redirects to the login page.
     *
     * @param response The HTTP servlet response to modify the cookies.
     * @return A response entity indicating the redirection to the logout success page.
     */
    @PostMapping("/api/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        Cookie invalidatedCookie = new Cookie("token", null);
        invalidatedCookie.setHttpOnly(true);
        invalidatedCookie.setSecure(false);
        invalidatedCookie.setPath("/");
        invalidatedCookie.setMaxAge(0);
        response.addCookie(invalidatedCookie);

        String redirectUrl = "/api/logout-success";
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(redirectUrl));
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    /**
     * Displays the logout success page.
     *
     * @return The name of the view to be rendered, which is the logout success page.
     */
    @GetMapping("/api/logout-success")
    public String getLogoutSuccess() {
        return "logout-success";
    }

    /**
     * Displays the logout page.
     *
     * @return The name of the view to be rendered, which is the logout page.
     */
    @GetMapping("/api/logout")
    public String getLogout() {
        return "logout" ;
    }
}
