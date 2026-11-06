package co.edu.unicauca.saberpro.domain.builder;

import co.edu.unicauca.saberpro.domain.exception.InvalidQuestionException;
import co.edu.unicauca.saberpro.domain.model.AnswerOption;
import co.edu.unicauca.saberpro.domain.model.Question;
import co.edu.unicauca.saberpro.domain.model.QuestionStatus;

import java.util.ArrayList;
import java.util.List;

public class QuestionBuilder implements IQuestionBuilder {

    public static final int REQUIRED_OPTIONS = 4;

    private Long id;
    private String statement;
    private String justification;
    private QuestionStatus status = QuestionStatus.DRAFT;
    private final List<AnswerOption> options = new ArrayList<>();

    @Override
    public IQuestionBuilder id(Long id) {
        this.id = id;
        return this;
    }

    @Override
    public IQuestionBuilder statement(String statement) {
        this.statement = statement;
        return this;
    }

    @Override
    public IQuestionBuilder justification(String justification) {
        this.justification = justification;
        return this;
    }

    @Override
    public IQuestionBuilder addOption(String text, boolean correct) {
        options.add(new AnswerOption(text, correct));
        return this;
    }

    @Override
    public IQuestionBuilder status(QuestionStatus status) {
        this.status = status;
        return this;
    }

    @Override
    public Question build() {
        List<String> errors = new ArrayList<>();
        if (statement == null || statement.isBlank()) {
            errors.add("El enunciado es obligatorio");
        }
        if (options.size() != REQUIRED_OPTIONS) {
            errors.add("La pregunta debe tener exactamente " + REQUIRED_OPTIONS + " opciones y tiene " + options.size());
        }
        if (options.stream().anyMatch(o -> o.text() == null || o.text().isBlank())) {
            errors.add("Ninguna opción puede estar vacía");
        }
        long correctas = options.stream().filter(AnswerOption::correct).count();
        if (correctas != 1) {
            errors.add("Debe haber exactamente 1 opción correcta y hay " + correctas);
        }
        if (status == null) {
            errors.add("El estado es obligatorio");
        }
        if (!errors.isEmpty()) {
            throw new InvalidQuestionException(errors);
        }
        return new Question(id, statement, justification, options, status.newState());
    }
}
