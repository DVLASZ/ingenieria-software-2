package com.example.questionbank.domain.model;

/**
 * Life cycle of a question. The allowed moves live here, so no other class decides them.
 */
public enum QuestionStatus {
    /** Being written by its author. */
    DRAFT,
    /** Sent to review, waiting for reviewers. */
    PENDING_REVIEW,
    /** Reviewers are evaluating it. */
    IN_REVIEW,
    /** Reviewers accepted it. */
    APPROVED,
    /** Reviewers refused it; it goes back to draft for corrections. */
    REJECTED,
    /** Available to be used in quizzes. */
    PUBLISHED,
    /** Retired; questions are never deleted. */
    ARCHIVED;

    /**
     * Tells whether a question in this status may move to another one.
     *
     * @param target the status the question would move to
     * @return true when the move is allowed
     */
    public boolean canMoveTo(QuestionStatus target) {
        if (this == ARCHIVED) {
            return false;
        }
        if (target == ARCHIVED) {
            return true;
        }
        return switch (this) {
            case DRAFT -> target == PENDING_REVIEW;
            case PENDING_REVIEW -> target == IN_REVIEW;
            case IN_REVIEW -> target == APPROVED || target == REJECTED;
            case REJECTED -> target == DRAFT;
            case APPROVED -> target == PUBLISHED;
            case PUBLISHED, ARCHIVED -> false;
        };
    }

    /**
     * Tells whether questions in this status can be used in quizzes.
     *
     * @return true only for {@link #PUBLISHED}
     */
    public boolean isPublished() {
        return this == PUBLISHED;
    }
}
