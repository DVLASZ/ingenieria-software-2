package com.example.questionbank.domain.repository;

import com.example.questionbank.domain.model.User;
import java.util.List;
import java.util.Optional;

/**
 * Persistence contract for users. It says what the application needs, not how it is stored.
 */
public interface UserRepository {

    /**
     * Stores a new user and returns it with its identity. A user cannot be changed once stored, so saving
     * one that already has identity does not update anything.
     *
     * @param user the user to store
     * @return the stored user
     */
    User save(User user);

    /**
     * Finds a user by identity.
     *
     * @param id the identity
     * @return the user, or empty when it does not exist
     */
    Optional<User> findById(Long id);

    /**
     * Finds a user by login name.
     *
     * @param username the login name
     * @return the user, or empty when it does not exist
     */
    Optional<User> findByUsername(String username);

    /**
     * Lists all users.
     *
     * @return every stored user
     */
    List<User> findAll();
}
