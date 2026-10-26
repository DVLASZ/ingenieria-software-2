package com.example.questionbank.infrastructure.jpa.record;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * JPA mapping of a competency. The code is unique.
 */
@Entity
@Table(name = "competencies")
public class CompetencyRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String code;

    @Column(nullable = false, length = 120)
    private String name;

    /** Required by JPA. */
    protected CompetencyRecord() {
    }

    /**
     * Creates a record to be inserted.
     *
     * @param code unique code
     * @param name readable name
     */
    public CompetencyRecord(String code, String name) {
        this.code = code;
        this.name = name;
    }

    /** {@return database identity} */
    public Long getId() {
        return id;
    }

    /** {@return unique code} */
    public String getCode() {
        return code;
    }

    /** {@return readable name} */
    public String getName() {
        return name;
    }
}
