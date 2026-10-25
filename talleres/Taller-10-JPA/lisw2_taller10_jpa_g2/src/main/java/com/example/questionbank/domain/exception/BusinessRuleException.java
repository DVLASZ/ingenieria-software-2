package com.example.questionbank.domain.exception;

/**
 * Thrown when an operation breaks a business rule of the question bank.
 */
public class BusinessRuleException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates the exception.
     *
     * @param message what rule was broken
     */
    public BusinessRuleException(String message) {
        super(message);
    }
}
