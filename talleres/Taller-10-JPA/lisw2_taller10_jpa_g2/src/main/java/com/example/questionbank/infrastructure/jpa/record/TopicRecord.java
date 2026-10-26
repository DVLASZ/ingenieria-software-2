package com.example.questionbank.infrastructure.jpa.record;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * JPA mapping of a topic. The name is unique.
 */
@Entity
@Table(name = "topics")
public class TopicRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 120)
    private String name;

    /** Required by JPA. */
    protected TopicRecord() {
    }

    /**
     * Creates a record to be inserted.
     *
     * @param name unique name
     */
    public TopicRecord(String name) {
        this.name = name;
    }

    /** {@return database identity} */
    public Long getId() {
        return id;
    }

    /** {@return unique name} */
    public String getName() {
        return name;
    }
}
