package com.example.questionbank.domain.model;

import com.example.questionbank.domain.exception.BusinessRuleException;

/**
 * Argument checks shared by the domain classes.
 */
final class Validation {

    private Validation() {
    }

    /** Returns the value or fails when it is null or blank. */
    static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new BusinessRuleException(field + " is required");
        }
        return value;
    }

    /** Returns the value or fails when it is null. */
    static <T> T requireNonNull(T value, String field) {
        if (value == null) {
            throw new BusinessRuleException(field + " is required");
        }
        return value;
    }
}
