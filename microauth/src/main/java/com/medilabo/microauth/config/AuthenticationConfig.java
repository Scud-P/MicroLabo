package com.medilabo.microauth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * The {@code AuthenticationConfig} class is a Spring configuration class that sets up
 * security for the application using Spring Security.
 * This class defines beans for user authentication, password encoding, and HTTP security
 * configurations to protect endpoints in the application.
 */
@Configuration
@EnableWebSecurity
public class AuthenticationConfig {

    /**
     * Creates a {@link UserDetailsService} bean to load user-specific data for authentication.
     *
     * @return an instance of {@link CustomUserDetailsService} that implements {@link UserDetailsService}
     */
    @Bean
    public UserDetailsService userDetailsService(){
        return new CustomUserDetailsService();
    }

    /**
     * Configures the {@link SecurityFilterChain} for the application.
     *
     * This method configures HTTP security settings</p>
     *
     * @param http the {@link HttpSecurity} object to configure
     * @return the configured {@link SecurityFilterChain}
     * @throws Exception if an error occurs during configuration
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/register", "/auth/token", "/auth/validate",
                                "/swagger-ui/**", "/v3/api-docs/**")
                        .permitAll()
                        .anyRequest().authenticated())
                .build();
    }

    /**
     * Creates a {@link PasswordEncoder} bean for encoding passwords.
     *
     * @return an instance of {@link BCryptPasswordEncoder}
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Creates an {@link AuthenticationProvider} bean for authenticating users.
     *
     * This provider uses the {@link UserDetailsService} and {@link PasswordEncoder}
     * to authenticate users based on their credentials.
     *
     * @return an instance of {@link DaoAuthenticationProvider}
     */
    @Bean
    public AuthenticationProvider authenticationProvider(){
        DaoAuthenticationProvider authenticationProvider=new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService());
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return authenticationProvider;
    }
    /**
     * Creates an {@link AuthenticationManager} bean for managing authentication processes.
     *
     * @param config the {@link AuthenticationConfiguration} used to create the manager
     * @return the configured {@link AuthenticationManager}
     * @throws Exception if an error occurs during the creation of the manager
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

}
