package com.example.questionbank.infrastructure.jpa.repository;

import com.example.questionbank.infrastructure.jpa.record.CompetencyRecord;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data interface for competencies.
 */
public interface SpringCompetencyDataRepository extends JpaRepository<CompetencyRecord, Long> {

    /**
     * Finds a competency by its code.
     *
     * @param code the code
     * @return the record, or empty
     */
    Optional<CompetencyRecord> findByCode(String code);
}
