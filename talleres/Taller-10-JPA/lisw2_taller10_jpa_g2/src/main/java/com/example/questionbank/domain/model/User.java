package com.example.questionbank.domain.model;

import com.example.questionbank.domain.exception.BusinessRuleException;
import java.util.Objects;

/**
 * A person who uses the question bank.
 */
public class User {

    private Long id;
    private final String username;
    private final String fullName;
    private final String email;
    private final Role role;

    /**
     * Creates a user.
     *
     * @param id identity, null until the repository saves it
     * @param username unique login name
     * @param fullName full name
     * @param email email address
     * @param role the role of the user
     */
    public User(Long id, String username, String fullName, String email, Role role) {
        this.id = id;
        this.username = Validation.requireText(username, "username");
        this.fullName = Validation.requireText(fullName, "fullName");
        Validation.requireText(email, "email");
        if (!email.contains("@") || email.startsWith("@") || email.endsWith("@")) {
            throw new BusinessRuleException("email is not valid");
        }
        this.email = email;
        this.role = Validation.requireNonNull(role, "role");
    }

    /** {@return the identity, or null when not saved yet} */
    public Long getId() {
        return id;
    }

    /**
     * Sets the identity once the repository has saved the user.
     *
     * @param id the identity
     */
    public void setId(Long id) {
        this.id = id;
    }

    /** {@return the login name} */
    public String getUsername() {
        return username;
    }

    /** {@return the full name} */
    public String getFullName() {
        return fullName;
    }

    /** {@return the email address} */
    public String getEmail() {
        return email;
    }

    /** {@return the role} */
    public Role getRole() {
        return role;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof User user && username.equals(user.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username);
    }
}
