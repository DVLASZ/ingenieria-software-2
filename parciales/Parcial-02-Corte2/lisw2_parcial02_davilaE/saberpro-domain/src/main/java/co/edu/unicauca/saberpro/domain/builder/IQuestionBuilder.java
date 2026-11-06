package co.edu.unicauca.saberpro.domain.builder;

import co.edu.unicauca.saberpro.domain.model.Question;
import co.edu.unicauca.saberpro.domain.model.QuestionStatus;

public interface IQuestionBuilder {

    IQuestionBuilder id(Long id);

    IQuestionBuilder statement(String statement);

    IQuestionBuilder justification(String justification);

    IQuestionBuilder addOption(String text, boolean correct);

    /** si no se llama, la pregunta nace en DRAFT */
    IQuestionBuilder status(QuestionStatus status);

    /** valida la estructura antes de crear la pregunta */
    Question build();
}
