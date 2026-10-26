package com.example.questionbank.infrastructure.jpa.record;

import com.example.questionbank.domain.model.Difficulty;
import com.example.questionbank.domain.model.QuestionStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * JPA mapping of a true/false question: one extra column in its own table.
 */
@Entity
@Table(name = "true_false_questions")
public class TrueFalseQuestionRecord extends QuestionRecord {

    @Column(name = "expected_answer", nullable = false)
    private boolean expectedAnswer;

    /** Required by JPA. */
    protected TrueFalseQuestionRecord() {
    }

    /**
     * Creates a record to be inserted.
     *
     * @param statement the statement to judge
     * @param difficulty how hard it is
     * @param status current status
     * @param author record of the author
     * @param expectedAnswer the right answer
     */
    public TrueFalseQuestionRecord(String statement, Difficulty difficulty, QuestionStatus status,
                                   UserRecord author, boolean expectedAnswer) {
        super(statement, difficulty, status, author);
        this.expectedAnswer = expectedAnswer;
    }

    /** {@return the right answer} */
    public boolean isExpectedAnswer() {
        return expectedAnswer;
    }
}
