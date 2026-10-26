package com.example.questionbank.infrastructure.jpa.repository;

import com.example.questionbank.infrastructure.jpa.record.TopicRecord;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data interface for topics.
 */
public interface SpringTopicDataRepository extends JpaRepository<TopicRecord, Long> {

    /**
     * Finds a topic by its name.
     *
     * @param name the name
     * @return the record, or empty
     */
    Optional<TopicRecord> findByName(String name);
}
