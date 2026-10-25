package com.example.questionbank.domain.model;

import java.util.Collection;

/**
 * A statement that students must mark as true or false.
 */
public class TrueFalseQuestion extends Question {

    private final boolean expectedAnswer;

    /**
     * Creates a new draft.
     *
     * @param id identity, null until the repository saves it
     * @param statement the statement to judge
     * @param difficulty how hard it is
     * @param author who writes it
     * @param expectedAnswer the right answer
     */
    public TrueFalseQuestion(Long id, String statement, Difficulty difficulty, User author,
                             boolean expectedAnswer) {
        super(id, statement, difficulty, author);
        this.expectedAnswer = expectedAnswer;
    }

    /**
     * Rebuilds a question in any state, for example when it comes back from the database.
     *
     * @param id identity
     * @param statement the statement to judge
     * @param difficulty how hard it is
     * @param status current status
     * @param author who wrote it
     * @param topic topic, may be null
     * @param competencies competencies it evaluates
     * @param metadata editorial data, may be null
     * @param expectedAnswer the right answer
     */
    public TrueFalseQuestion(Long id, String statement, Difficulty difficulty, QuestionStatus status, User author,
                             Topic topic, Collection<Competency> competencies, QuestionMetadata metadata,
                             boolean expectedAnswer) {
        super(id, statement, difficulty, status, author, topic, competencies, metadata);
        this.expectedAnswer = expectedAnswer;
    }

    @Override
    public QuestionType getType() {
        return QuestionType.TRUE_FALSE;
    }

    /** {@return the right answer} */
    public boolean isExpectedAnswer() {
        return expectedAnswer;
    }

    @Override
    protected void validateContent() {
        // the statement is already checked in the base class and the answer cannot be missing
    }
}
