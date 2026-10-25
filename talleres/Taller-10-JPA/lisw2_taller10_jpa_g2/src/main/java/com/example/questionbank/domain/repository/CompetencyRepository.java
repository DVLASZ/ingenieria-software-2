package com.example.questionbank.domain.repository;

import com.example.questionbank.domain.model.Competency;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Persistence contract for competencies.
 */
public interface CompetencyRepository {

    /**
     * Stores a new competency and returns it with its identity. A competency cannot be changed once stored, so saving
     * one that already has identity does not update anything.
     *
     * @param competency the competency to store
     * @return the stored competency
     */
    Competency save(Competency competency);

    /**
     * Finds a competency by identity.
     *
     * @param id the identity
     * @return the competency, or empty when it does not exist
     */
    Optional<Competency> findById(Long id);

    /**
     * Finds a competency by its unique code.
     *
     * @param code the code
     * @return the competency, or empty when it does not exist
     */
    Optional<Competency> findByCode(String code);

    /**
     * Finds the competencies with the given identities. Identities that do not exist are skipped.
     *
     * @param ids the identities
     * @return the competencies found
     */
    List<Competency> findAllById(Collection<Long> ids);

    /**
     * Lists all competencies.
     *
     * @return every stored competency
     */
    List<Competency> findAll();
}
