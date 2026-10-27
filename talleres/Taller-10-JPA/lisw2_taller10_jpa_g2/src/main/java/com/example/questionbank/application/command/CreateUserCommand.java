package com.example.questionbank.application.command;

import com.example.questionbank.domain.model.Role;

/**
 * Data needed to create a user.
 *
 * @param username unique login name
 * @param fullName full name
 * @param email email address
 * @param role the role of the user
 */
public record CreateUserCommand(String username, String fullName, String email, Role role) {
}
