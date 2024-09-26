package com.medilabo.microauth.repository;

import com.medilabo.microauth.entity.UserCredentials;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for managing {@link UserCredentials} entities.
 * <p>
 * This interface extends {@link JpaRepository} to provide CRUD operations
 * for the UserCredentials entity. It also defines custom methods for
 * querying user credentials by specific attributes.
 * </p>
 */
@Repository
public interface UserCredentialsRepository extends JpaRepository<UserCredentials, Integer> {
    /**
     * Finds a {@link UserCredentials} entity by its username.
     *
     * @param name the username of the user
     * @return an {@link Optional} containing the found UserCredentials,
     *         or an empty Optional if no user with the given username exists
     */
    Optional<UserCredentials> findByName(String name);
}
