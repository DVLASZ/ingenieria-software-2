package com.example.questionbank.application.command;

import com.example.questionbank.domain.model.Difficulty;
import com.example.questionbank.domain.model.QuestionMetadata;

/**
 * Data needed to create a true/false question.
 *
 * @param statement the statement to judge
 * @param difficulty how hard it is
 * @param authorId identity of the author
 * @param expectedAnswer the right answer
 * @param metadata editorial data, may be null
 */
public record CreateTrueFalseQuestionCommand(String statement, Difficulty difficulty, Long authorId,
                                             boolean expectedAnswer, QuestionMetadata metadata) {
}
