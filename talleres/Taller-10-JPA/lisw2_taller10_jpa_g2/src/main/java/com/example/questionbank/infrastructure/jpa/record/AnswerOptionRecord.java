package com.example.questionbank.infrastructure.jpa.record;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * JPA mapping of an answer option. The foreign key {@code question_id} is mandatory, so an option cannot exist
 * without its question.
 */
@Entity
@Table(name = "answer_options")
public class AnswerOptionRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private MultipleChoiceQuestionRecord question;

    @Column(nullable = false, length = 500)
    private String text;

    @Column(nullable = false)
    private boolean correct;

    /** Required by JPA. */
    protected AnswerOptionRecord() {
    }

    /**
     * Creates a record to be inserted.
     *
     * @param text what the option says
     * @param correct whether it is the right answer
     */
    public AnswerOptionRecord(String text, boolean correct) {
        this.text = text;
        this.correct = correct;
    }

    /** {@return database identity} */
    public Long getId() {
        return id;
    }

    /** {@return the question that owns this option} */
    public MultipleChoiceQuestionRecord getQuestion() {
        return question;
    }

    /**
     * Sets the question that owns this option.
     *
     * @param question the question that owns this option
     */
    void setQuestion(MultipleChoiceQuestionRecord question) {
        this.question = question;
    }

    /** {@return what the option says} */
    public String getText() {
        return text;
    }

    /** {@return whether it is the right answer} */
    public boolean isCorrect() {
        return correct;
    }
}
