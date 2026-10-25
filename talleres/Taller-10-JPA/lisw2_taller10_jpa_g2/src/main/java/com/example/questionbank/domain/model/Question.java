package com.example.questionbank.domain.model;

import com.example.questionbank.domain.exception.BusinessRuleException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Base class of every question in the bank. It knows its own life cycle rules and nothing about how it is stored.
 */
public abstract class Question {

    private Long id;
    private String statement;
    private final Difficulty difficulty;
    private QuestionStatus status;
    private final User author;
    private Topic topic;
    private final Set<Competency> competencies = new LinkedHashSet<>();
    private QuestionMetadata metadata;

    /**
     * Creates a new draft.
     *
     * @param id identity, null until the repository saves it
     * @param statement what the question asks
     * @param difficulty how hard it is
     * @param author who writes it
     */
    protected Question(Long id, String statement, Difficulty difficulty, User author) {
        this(id, statement, difficulty, QuestionStatus.DRAFT, author, null, Collections.emptySet(), null);
    }

    /**
     * Rebuilds a question in any state, for example when it comes back from the database.
     *
     * @param id identity
     * @param statement what the question asks
     * @param difficulty how hard it is
     * @param status current status
     * @param author who wrote it
     * @param topic topic, may be null
     * @param competencies competencies it evaluates, may be empty
     * @param metadata editorial data, may be null
     */
    protected Question(Long id, String statement, Difficulty difficulty, QuestionStatus status, User author,
                       Topic topic, Collection<Competency> competencies, QuestionMetadata metadata) {
        this.id = id;
        this.statement = Validation.requireText(statement, "statement");
        this.difficulty = Validation.requireNonNull(difficulty, "difficulty");
        this.status = Validation.requireNonNull(status, "status");
        this.author = Validation.requireNonNull(author, "author");
        this.topic = topic;
        this.competencies.addAll(competencies);
        this.metadata = metadata;
    }

    /** {@return which kind of question this is} */
    public abstract QuestionType getType();

    /**
     * Checks the content that is specific to the kind of question. It runs when the question is sent to review.
     *
     * @throws BusinessRuleException when the content is not valid
     */
    protected abstract void validateContent();

    /**
     * Sets the topic and the competencies of the question. Only drafts can be classified.
     *
     * @param topic the topic
     * @param competencies one or more competencies
     */
    public void classify(Topic topic, Collection<Competency> competencies) {
        requireDraft("classify");
        Validation.requireNonNull(topic, "topic");
        if (competencies == null || competencies.isEmpty()) {
            throw new BusinessRuleException("at least one competency is required");
        }
        this.topic = topic;
        this.competencies.clear();
        this.competencies.addAll(competencies);
    }

    /**
     * Changes the statement. Only drafts can be edited.
     *
     * @param newStatement the new statement
     */
    public void updateStatement(String newStatement) {
        requireDraft("edit the statement of");
        this.statement = Validation.requireText(newStatement, "statement");
    }

    /**
     * Stores the editorial metadata. It can change in any status, because editors update it during the review.
     *
     * @param newMetadata the metadata
     */
    public void attachMetadata(QuestionMetadata newMetadata) {
        this.metadata = Validation.requireNonNull(newMetadata, "metadata");
    }

    /**
     * Sends the draft to review. The question needs a topic, at least one competency and valid content.
     * If any condition fails, the status does not change.
     */
    public void submitForReview() {
        if (!status.canMoveTo(QuestionStatus.PENDING_REVIEW)) {
            throw new BusinessRuleException("Only drafts can be sent to review, but the question is " + status);
        }
        if (topic == null) {
            throw new BusinessRuleException("The question needs a topic before it is sent to review");
        }
        if (competencies.isEmpty()) {
            throw new BusinessRuleException("The question needs at least one competency before it is sent to review");
        }
        validateContent();
        status = QuestionStatus.PENDING_REVIEW;
    }

    /**
     * Moves the question to another status, following the life cycle rules.
     *
     * @param target the new status
     */
    public void moveTo(QuestionStatus target) {
        Validation.requireNonNull(target, "target status");
        if (target == QuestionStatus.PENDING_REVIEW) {
            submitForReview();
            return;
        }
        if (!status.canMoveTo(target)) {
            throw new BusinessRuleException("Cannot move a question from " + status + " to " + target);
        }
        status = target;
    }

    /** {@return true when the question can be used in quizzes} */
    public boolean isPublished() {
        return status.isPublished();
    }

    /**
     * Fails unless the question is still a draft.
     *
     * @param action the verb phrase for the error message, for example "classify"
     */
    protected void requireDraft(String action) {
        if (status != QuestionStatus.DRAFT) {
            throw new BusinessRuleException("Cannot " + action + " a question that is " + status);
        }
    }

    /** {@return the identity, or null when not saved yet} */
    public Long getId() {
        return id;
    }

    /**
     * Sets the identity once the repository has saved the question.
     *
     * @param id the identity
     */
    public void setId(Long id) {
        this.id = id;
    }

    /** {@return what the question asks} */
    public String getStatement() {
        return statement;
    }

    /** {@return how hard the question is} */
    public Difficulty getDifficulty() {
        return difficulty;
    }

    /** {@return the current status} */
    public QuestionStatus getStatus() {
        return status;
    }

    /** {@return who wrote the question} */
    public User getAuthor() {
        return author;
    }

    /** {@return the topic, or null when the question is not classified} */
    public Topic getTopic() {
        return topic;
    }

    /** {@return the competencies, as a read-only set} */
    public Set<Competency> getCompetencies() {
        return Collections.unmodifiableSet(competencies);
    }

    /** {@return the editorial metadata, or null when there is none} */
    public QuestionMetadata getMetadata() {
        return metadata;
    }
}
