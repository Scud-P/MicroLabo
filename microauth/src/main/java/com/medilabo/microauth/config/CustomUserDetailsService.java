package com.medilabo.microauth.config;

import com.medilabo.microauth.entity.UserCredentials;
import com.medilabo.microauth.repository.UserCredentialsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * The {@code CustomUserDetailsService} class implements the {@link UserDetailsService} interface
 * to provide custom user details for authentication.
 *
 * This service is responsible for loading user-specific data from a database using the
 * {@link UserCredentialsRepository} and returning an instance of {@link UserDetails}.
 */
@Component
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserCredentialsRepository userCredentialsRepository;

    /**
     * Loads a user by the given username.
     * This method retrieves user credentials from the database using the provided username.
     * If the user is found, a {@link CustomUserDetails} object is returned. If not, a
     * {@link UsernameNotFoundException} is thrown.
     *
     * @param username the username of the user to be loaded
     * @return an instance of {@link UserDetails} representing the user
     * @throws UsernameNotFoundException if the user with the given username cannot be found
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<UserCredentials> credentials = userCredentialsRepository.findByName(username);
        return credentials.map(CustomUserDetails::new).orElseThrow(() -> new UsernameNotFoundException("user not found with name :" + username));
    }
}
