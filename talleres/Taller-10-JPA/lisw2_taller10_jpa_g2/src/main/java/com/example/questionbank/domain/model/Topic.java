package com.example.questionbank.domain.model;

import java.util.Objects;

/**
 * A subject that groups questions.
 */
public class Topic {

    private Long id;
    private final String name;

    /**
     * Creates a topic.
     *
     * @param id identity, null until the repository saves it
     * @param name unique name
     */
    public Topic(Long id, String name) {
        this.id = id;
        this.name = Validation.requireText(name, "name");
    }

    /** {@return the identity, or null when not saved yet} */
    public Long getId() {
        return id;
    }

    /**
     * Sets the identity once the repository has saved the topic.
     *
     * @param id the identity
     */
    public void setId(Long id) {
        this.id = id;
    }

    /** {@return the name} */
    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Topic topic && name.equals(topic.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
