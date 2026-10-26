package com.example.questionbank.infrastructure.jpa.record;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import org.hibernate.annotations.BatchSize;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA mapping of a quiz. The questions go in the join table {@code quiz_questions}, which keeps their order.
 */
@Entity
@Table(name = "quizzes")
public class QuizRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Counter that Hibernate checks on every update. */
    @Version
    private Long version;

    @Column(nullable = false, length = 160)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private UserRecord owner;

    @ManyToMany
    @JoinTable(name = "quiz_questions",
            joinColumns = @JoinColumn(name = "quiz_id"),
            inverseJoinColumns = @JoinColumn(name = "question_id"))
    @OrderColumn(name = "position")
    @BatchSize(size = 50)
    private List<QuestionRecord> questions = new ArrayList<>();

    /** Required by JPA. */
    protected QuizRecord() {
    }

    /**
     * Creates a record to be inserted.
     *
     * @param title the title
     * @param owner record of the teacher who owns the quiz
     */
    public QuizRecord(String title, UserRecord owner) {
        this.title = title;
        this.owner = owner;
    }

    /** {@return database identity} */
    public Long getId() {
        return id;
    }

    /** {@return the title} */
    public String getTitle() {
        return title;
    }

    /** {@return the owner record} */
    public UserRecord getOwner() {
        return owner;
    }

    /** {@return the question records, in order} */
    public List<QuestionRecord> getQuestions() {
        return questions;
    }
}
