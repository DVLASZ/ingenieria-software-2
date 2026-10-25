package com.example.questionbank.domain.repository;

import com.example.questionbank.domain.model.Topic;
import java.util.List;
import java.util.Optional;

/**
 * Persistence contract for topics.
 */
public interface TopicRepository {

    /**
     * Stores a new topic and returns it with its identity. A topic cannot be changed once stored, so saving
     * one that already has identity does not update anything.
     *
     * @param topic the topic to store
     * @return the stored topic
     */
    Topic save(Topic topic);

    /**
     * Finds a topic by identity.
     *
     * @param id the identity
     * @return the topic, or empty when it does not exist
     */
    Optional<Topic> findById(Long id);

    /**
     * Finds a topic by its unique name.
     *
     * @param name the name
     * @return the topic, or empty when it does not exist
     */
    Optional<Topic> findByName(String name);

    /**
     * Lists all topics.
     *
     * @return every stored topic
     */
    List<Topic> findAll();
}
