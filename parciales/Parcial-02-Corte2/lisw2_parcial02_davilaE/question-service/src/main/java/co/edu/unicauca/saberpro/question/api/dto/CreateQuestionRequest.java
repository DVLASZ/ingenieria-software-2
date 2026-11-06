package co.edu.unicauca.saberpro.question.api.dto;

import co.edu.unicauca.saberpro.domain.model.AnswerOption;
import co.edu.unicauca.saberpro.question.application.command.CreateQuestionCommand;

import java.util.List;

public record CreateQuestionRequest(String statement, String justification, List<OptionRequest> options) {

    public CreateQuestionCommand toCommand() {
        List<AnswerOption> mapped = options == null ? List.of()
                : options.stream().map(o -> new AnswerOption(o.text(), o.correct())).toList();
        return new CreateQuestionCommand(statement, justification, mapped);
    }
}
