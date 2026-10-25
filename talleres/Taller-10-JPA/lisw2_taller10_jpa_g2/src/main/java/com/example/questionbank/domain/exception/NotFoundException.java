package com.example.questionbank.domain.exception;

/**
 * Thrown when an entity that an operation refers to does not exist.
 */
public class NotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates the exception.
     *
     * @param message what was not found
     */
    public NotFoundException(String message) {
        super(message);
    }
}
