package com.example.questionbank.domain.model;

import java.util.Objects;

/**
 * A competency evaluated by the Saber Pro exam, for example critical reading.
 */
public class Competency {

    private Long id;
    private final String code;
    private final String name;

    /**
     * Creates a competency.
     *
     * @param id identity, null until the repository saves it
     * @param code short unique code
     * @param name readable name
     */
    public Competency(Long id, String code, String name) {
        this.id = id;
        this.code = Validation.requireText(code, "code");
        this.name = Validation.requireText(name, "name");
    }

    /** {@return the identity, or null when not saved yet} */
    public Long getId() {
        return id;
    }

    /**
     * Sets the identity once the repository has saved the competency.
     *
     * @param id the identity
     */
    public void setId(Long id) {
        this.id = id;
    }

    /** {@return the unique code} */
    public String getCode() {
        return code;
    }

    /** {@return the readable name} */
    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Competency competency && code.equals(competency.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }
}
