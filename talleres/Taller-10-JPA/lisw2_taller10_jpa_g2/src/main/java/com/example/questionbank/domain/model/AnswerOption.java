package com.example.questionbank.domain.model;

/**
 * One option of a multiple choice question.
 *
 * @param text what the option says
 * @param correct whether this is the right answer
 */
public record AnswerOption(String text, boolean correct) {

    /**
     * Checks that the option has text.
     */
    public AnswerOption {
        Validation.requireText(text, "option text");
    }
}
