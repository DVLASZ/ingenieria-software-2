package com.example.questionbank.infrastructure.jpa.mapper;

import com.example.questionbank.domain.model.Topic;
import com.example.questionbank.infrastructure.jpa.record.TopicRecord;

/**
 * Converts topics between the domain model and the JPA record.
 */
public final class TopicMapper {

    private TopicMapper() {
    }

    /**
     * Builds the domain topic from a record.
     *
     * @param record the JPA record
     * @return the domain topic
     */
    public static Topic toDomain(TopicRecord record) {
        return new Topic(record.getId(), record.getName());
    }

    /**
     * Builds a new record from a domain topic.
     *
     * @param topic the domain topic
     * @return a record ready to be inserted
     */
    public static TopicRecord toRecord(Topic topic) {
        return new TopicRecord(topic.getName());
    }
}
