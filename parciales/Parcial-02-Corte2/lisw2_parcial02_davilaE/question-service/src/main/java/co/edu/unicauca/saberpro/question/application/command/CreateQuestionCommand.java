package co.edu.unicauca.saberpro.question.application.command;

import co.edu.unicauca.saberpro.domain.model.AnswerOption;

import java.util.List;

public record CreateQuestionCommand(String statement, String justification, List<AnswerOption> options) {
}
