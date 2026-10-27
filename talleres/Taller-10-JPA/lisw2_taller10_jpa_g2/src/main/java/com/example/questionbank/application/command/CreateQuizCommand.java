package com.example.questionbank.application.command;

import java.util.List;

/**
 * Data needed to create a quiz.
 *
 * @param title the title
 * @param ownerId identity of the teacher who owns the quiz
 * @param questionIds identities of the published questions to include, in order
 */
public record CreateQuizCommand(String title, Long ownerId, List<Long> questionIds) {
}
