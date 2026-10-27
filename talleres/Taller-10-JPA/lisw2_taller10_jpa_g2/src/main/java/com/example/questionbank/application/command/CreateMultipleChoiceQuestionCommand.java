package com.example.questionbank.application.command;

import com.example.questionbank.domain.model.AnswerOption;
import com.example.questionbank.domain.model.Difficulty;
import com.example.questionbank.domain.model.QuestionMetadata;
import java.util.List;

/**
 * Data needed to create a multiple choice question.
 *
 * @param statement what the question asks
 * @param difficulty how hard it is
 * @param authorId identity of the author
 * @param options the options, correct and wrong ones
 * @param metadata editorial data, may be null
 */
public record CreateMultipleChoiceQuestionCommand(String statement, Difficulty difficulty, Long authorId,
                                                  List<AnswerOption> options, QuestionMetadata metadata) {
}
