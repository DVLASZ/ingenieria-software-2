package co.edu.unicauca.saberpro.question.api.dto;

import co.edu.unicauca.saberpro.domain.model.Question;

import java.util.List;

public record QuestionResponse(Long id, String statement, String justification, List<OptionRequest> options,
                               String status) {

    public static QuestionResponse from(Question question) {
        return new QuestionResponse(question.getId(), question.getStatement(), question.getJustification(),
                question.getOptions().stream().map(o -> new OptionRequest(o.text(), o.correct())).toList(),
                question.getStatus().name());
    }
}
