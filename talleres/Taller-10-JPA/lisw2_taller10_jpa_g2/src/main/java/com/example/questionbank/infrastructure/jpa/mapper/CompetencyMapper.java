package com.example.questionbank.infrastructure.jpa.mapper;

import com.example.questionbank.domain.model.Competency;
import com.example.questionbank.infrastructure.jpa.record.CompetencyRecord;

/**
 * Converts competencies between the domain model and the JPA record.
 */
public final class CompetencyMapper {

    private CompetencyMapper() {
    }

    /**
     * Builds the domain competency from a record.
     *
     * @param record the JPA record
     * @return the domain competency
     */
    public static Competency toDomain(CompetencyRecord record) {
        return new Competency(record.getId(), record.getCode(), record.getName());
    }

    /**
     * Builds a new record from a domain competency.
     *
     * @param competency the domain competency
     * @return a record ready to be inserted
     */
    public static CompetencyRecord toRecord(Competency competency) {
        return new CompetencyRecord(competency.getCode(), competency.getName());
    }
}
