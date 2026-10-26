package com.example.questionbank.infrastructure.jpa.record;

import com.example.questionbank.domain.model.Difficulty;
import com.example.questionbank.domain.model.QuestionStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import org.hibernate.annotations.BatchSize;
import java.util.HashSet;
import java.util.Set;

/**
 * JPA mapping of the question hierarchy. With the JOINED strategy this class owns the common columns in
 * the {@code questions} table, and each subtype adds its own table.
 */
@Entity
@Table(name = "questions")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class QuestionRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Counter that Hibernate checks on every update, so two concurrent changes cannot overwrite each other. */
    @Version
    private Long version;

    @Column(nullable = false, length = 2000)
    private String statement;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Difficulty difficulty;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private QuestionStatus status;

    /** Many questions to one author; this side owns the foreign key {@code author_id}. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private UserRecord author;

    /** Many questions to one topic; the topic is optional until the question is classified. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id")
    private TopicRecord topic;

    /** Many to many with competencies, through the join table {@code question_competencies}. */
    @ManyToMany
    @OrderBy("code")
    @BatchSize(size = 50)
    @JoinTable(name = "question_competencies",
            joinColumns = @JoinColumn(name = "question_id"),
            inverseJoinColumns = @JoinColumn(name = "competency_id"))
    private Set<CompetencyRecord> competencies = new HashSet<>();

    /** One to one with the metadata; the foreign key lives in the metadata table. */
    @OneToOne(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private QuestionMetadataRecord metadata;

    /** Required by JPA. */
    protected QuestionRecord() {
    }

    /**
     * Creates the common part of a record to be inserted.
     *
     * @param statement what the question asks
     * @param difficulty how hard it is
     * @param status current status
     * @param author record of the author
     */
    protected QuestionRecord(String statement, Difficulty difficulty, QuestionStatus status, UserRecord author) {
        this.statement = statement;
        this.difficulty = difficulty;
        this.status = status;
        this.author = author;
    }

    /** {@return database identity} */
    public Long getId() {
        return id;
    }

    /** {@return what the question asks} */
    public String getStatement() {
        return statement;
    }

    /**
     * Sets what the question asks.
     *
     * @param statement what the question asks
     */
    public void setStatement(String statement) {
        this.statement = statement;
    }

    /** {@return how hard the question is} */
    public Difficulty getDifficulty() {
        return difficulty;
    }

    /** {@return current status} */
    public QuestionStatus getStatus() {
        return status;
    }

    /**
     * Sets the new status.
     *
     * @param status the new status
     */
    public void setStatus(QuestionStatus status) {
        this.status = status;
    }

    /** {@return the author record} */
    public UserRecord getAuthor() {
        return author;
    }

    /** {@return the topic record, or null} */
    public TopicRecord getTopic() {
        return topic;
    }

    /**
     * Sets the topic record, or null.
     *
     * @param topic the topic record, or null
     */
    public void setTopic(TopicRecord topic) {
        this.topic = topic;
    }

    /** {@return the competency records} */
    public Set<CompetencyRecord> getCompetencies() {
        return competencies;
    }

    /** {@return the metadata record, or null} */
    public QuestionMetadataRecord getMetadata() {
        return metadata;
    }

    /**
     * Sets the metadata record, or null to remove it.
     *
     * @param metadata the metadata record, or null to remove it
     */
    public void setMetadata(QuestionMetadataRecord metadata) {
        this.metadata = metadata;
    }
}
