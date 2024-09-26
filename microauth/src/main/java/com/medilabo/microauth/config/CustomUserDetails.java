package com.medilabo.microauth.config;

import com.medilabo.microauth.entity.UserCredentials;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

/**
 * Custom implementation of Spring Security's {@link UserDetails} interface.
 * This class encapsulates user credentials and provides necessary user
 * details for authentication and authorization processes.
 */
public class CustomUserDetails implements UserDetails {

    private String username;
    private String password;

    /**
     * Constructs a {@link CustomUserDetails} object from the provided
     * {@link UserCredentials}.
     *
     * @param userCredentials the user credentials containing username and password
     */
    public CustomUserDetails(UserCredentials userCredentials) {
        this.username = userCredentials.getName();
        this.password = userCredentials.getPassword();
    }

    /**
     * Returns the authorities granted to the user.
     *
     * @return a collection of {@link GrantedAuthority} objects, or null if none
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    /**
     * Returns the password of the user.
     *
     * @return the user's password
     */
    @Override
    public String getPassword() {
        return password;
    }

    /**
     * Returns the username of the user.
     *
     * @return the user's username
     */
    @Override
    public String getUsername() {
        return username;
    }

    /**
     * Indicates whether the user's account is non-expired.
     *
     * @return true if the user's account is valid; false otherwise
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Indicates whether the user is non-locked.
     *
     * @return true if the user's account is not locked; false otherwise
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * Indicates whether the user's credentials are non-expired.
     *
     * @return true if the user's credentials are valid; false otherwise
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Indicates whether the user is enabled.
     *
     * @return true if the user is enabled; false otherwise
     */
    @Override
    public boolean isEnabled() {
        return true;
    }
}
