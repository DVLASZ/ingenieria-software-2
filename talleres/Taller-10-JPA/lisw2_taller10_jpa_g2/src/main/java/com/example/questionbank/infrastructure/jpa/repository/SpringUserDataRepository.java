package com.example.questionbank.infrastructure.jpa.repository;

import com.example.questionbank.infrastructure.jpa.record.UserRecord;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data interface for users. Only the JPA repositories of the infrastructure use it.
 */
public interface SpringUserDataRepository extends JpaRepository<UserRecord, Long> {

    /**
     * Finds a user by login name.
     *
     * @param username the login name
     * @return the record, or empty
     */
    Optional<UserRecord> findByUsername(String username);
}
