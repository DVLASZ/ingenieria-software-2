package com.example.questionbank.infrastructure.jpa.mapper;

import com.example.questionbank.domain.model.User;
import com.example.questionbank.infrastructure.jpa.record.UserRecord;

/**
 * Converts users between the domain model and the JPA record.
 */
public final class UserMapper {

    private UserMapper() {
    }

    /**
     * Builds the domain user from a record.
     *
     * @param record the JPA record
     * @return the domain user
     */
    public static User toDomain(UserRecord record) {
        return new User(record.getId(), record.getUsername(), record.getFullName(), record.getEmail(),
                record.getRole());
    }

    /**
     * Builds a new record from a domain user.
     *
     * @param user the domain user
     * @return a record ready to be inserted
     */
    public static UserRecord toRecord(User user) {
        return new UserRecord(user.getUsername(), user.getFullName(), user.getEmail(), user.getRole());
    }
}
