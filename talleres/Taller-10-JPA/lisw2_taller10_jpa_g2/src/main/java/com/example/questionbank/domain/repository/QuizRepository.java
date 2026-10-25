package com.example.questionbank.domain.repository;

import com.example.questionbank.domain.model.Quiz;
import java.util.List;
import java.util.Optional;

/**
 * Persistence contract for quizzes.
 */
public interface QuizRepository {

    /**
     * Stores a quiz. A quiz without identity is inserted; one with identity is updated.
     *
     * @param quiz the quiz to store
     * @return the stored quiz, with identities assigned
     */
    Quiz save(Quiz quiz);

    /**
     * Finds a quiz by identity.
     *
     * @param id the identity
     * @return the quiz, or empty when it does not exist
     */
    Optional<Quiz> findById(Long id);

    /**
     * Lists all quizzes.
     *
     * @return every stored quiz
     */
    List<Quiz> findAll();
}
