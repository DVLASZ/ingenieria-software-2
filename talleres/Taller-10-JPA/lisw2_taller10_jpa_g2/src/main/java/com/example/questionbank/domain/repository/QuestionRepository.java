package com.example.questionbank.domain.repository;

import com.example.questionbank.domain.model.Question;
import com.example.questionbank.domain.model.QuestionStatus;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Persistence contract for questions of any type. Nothing here mentions JPA or Spring.
 */
public interface QuestionRepository {

    /**
     * Stores a question. A question without identity is inserted; one with identity is updated.
     *
     * @param question the question to store
     * @return the stored question, with identities assigned
     */
    Question save(Question question);

    /**
     * Finds a question by identity.
     *
     * @param id the identity
     * @return the question, or empty when it does not exist
     */
    Optional<Question> findById(Long id);

    /**
     * Finds the questions with the given identities. Identities that do not exist are skipped.
     *
     * @param ids the identities
     * @return the questions found
     */
    List<Question> findAllById(Collection<Long> ids);

    /**
     * Lists all questions.
     *
     * @return every stored question
     */
    List<Question> findAll();

    /**
     * Lists the questions written by an author.
     *
     * @param authorId identity of the author
     * @return the questions of that author
     */
    List<Question> findByAuthorId(Long authorId);

    /**
     * Lists the questions in a given status.
     *
     * @param status the status
     * @return the questions in that status
     */
    List<Question> findByStatus(QuestionStatus status);

    /**
     * Lists the published questions that evaluate a competency.
     *
     * @param competencyId identity of the competency
     * @return the published questions of that competency
     */
    List<Question> findPublishedByCompetencyId(Long competencyId);
}
