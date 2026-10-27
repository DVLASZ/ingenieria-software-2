package com.example.questionbank.api.dto;

import com.example.questionbank.domain.model.Role;
import com.example.questionbank.domain.model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request and response objects for users.
 */
public final class UserDtos {

    private UserDtos() {
    }

    /**
     * Body to create a user.
     *
     * @param username unique login name
     * @param fullName full name
     * @param email email address
     * @param role role of the user
     */
    public record CreateUserRequest(@NotBlank @Size(max = 60) String username,
                                    @NotBlank @Size(max = 120) String fullName,
                                    @NotBlank @Email @Size(max = 120) String email, @NotNull Role role) {
    }

    /**
     * A user as the API shows it.
     *
     * @param id identity
     * @param username login name
     * @param fullName full name
     * @param email email address
     * @param role role of the user
     */
    public record UserResponse(Long id, String username, String fullName, String email, Role role) {

        /**
         * Builds the response from a domain user.
         *
         * @param user the domain user
         * @return the response
         */
        public static UserResponse from(User user) {
            return new UserResponse(user.getId(), user.getUsername(), user.getFullName(), user.getEmail(),
                    user.getRole());
        }
    }
}
