package com.example.questionbank.infrastructure.jpa.record;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;

/**
 * JPA mapping of the question metadata. This side owns the one to one association: {@code question_id} is
 * unique and not null, so every metadata row belongs to exactly one question and a question has at most one.
 */
@Entity
@Table(name = "question_metadata")
public class QuestionMetadataRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false, unique = true)
    private QuestionRecord question;

    @Column(name = "cognitive_level", nullable = false, length = 60)
    private String cognitiveLevel;

    @Column(name = "estimated_seconds", nullable = false)
    private int estimatedSeconds;

    @Column(name = "editorial_notes", length = 1000)
    private String editorialNotes;

    @Column(name = "last_reviewed_at")
    private LocalDate lastReviewedAt;

    /** Required by JPA. */
    protected QuestionMetadataRecord() {
    }

    /**
     * Creates a record to be inserted.
     *
     * @param question the question it belongs to
     * @param cognitiveLevel cognitive level
     * @param estimatedSeconds estimated answering time in seconds
     * @param editorialNotes editorial notes, may be null
     * @param lastReviewedAt day of the last review, may be null
     */
    public QuestionMetadataRecord(QuestionRecord question, String cognitiveLevel, int estimatedSeconds,
                                  String editorialNotes, LocalDate lastReviewedAt) {
        this.question = question;
        this.cognitiveLevel = cognitiveLevel;
        this.estimatedSeconds = estimatedSeconds;
        this.editorialNotes = editorialNotes;
        this.lastReviewedAt = lastReviewedAt;
    }

    /** {@return database identity} */
    public Long getId() {
        return id;
    }

    /** {@return the question it belongs to} */
    public QuestionRecord getQuestion() {
        return question;
    }

    /** {@return cognitive level} */
    public String getCognitiveLevel() {
        return cognitiveLevel;
    }

    /** {@return estimated answering time in seconds} */
    public int getEstimatedSeconds() {
        return estimatedSeconds;
    }

    /** {@return editorial notes, may be null} */
    public String getEditorialNotes() {
        return editorialNotes;
    }

    /** {@return day of the last review, may be null} */
    public LocalDate getLastReviewedAt() {
        return lastReviewedAt;
    }

    /**
     * Updates the editorial data in place.
     *
     * @param cognitiveLevel cognitive level
     * @param estimatedSeconds estimated answering time in seconds
     * @param editorialNotes editorial notes, may be null
     * @param lastReviewedAt day of the last review, may be null
     */
    public void update(String cognitiveLevel, int estimatedSeconds, String editorialNotes,
                       LocalDate lastReviewedAt) {
        this.cognitiveLevel = cognitiveLevel;
        this.estimatedSeconds = estimatedSeconds;
        this.editorialNotes = editorialNotes;
        this.lastReviewedAt = lastReviewedAt;
    }
}
