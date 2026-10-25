package com.example.questionbank.domain.model;

import com.example.questionbank.domain.exception.BusinessRuleException;
import java.time.LocalDate;

/**
 * Editorial data that complements a question.
 *
 * @param cognitiveLevel cognitive level the question asks for
 * @param estimatedSeconds time a student needs to answer, in seconds
 * @param editorialNotes free notes from the editors, may be null
 * @param lastReviewedAt day of the last review, may be null
 */
public record QuestionMetadata(String cognitiveLevel, int estimatedSeconds, String editorialNotes,
                               LocalDate lastReviewedAt) {

    /**
     * Checks the level and the estimated time.
     */
    public QuestionMetadata {
        Validation.requireText(cognitiveLevel, "cognitive level");
        if (estimatedSeconds <= 0) {
            throw new BusinessRuleException("estimated time must be positive");
        }
    }
}
