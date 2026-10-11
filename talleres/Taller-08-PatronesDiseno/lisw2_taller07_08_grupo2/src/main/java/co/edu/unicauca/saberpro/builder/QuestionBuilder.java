package co.edu.unicauca.saberpro.builder;

import co.edu.unicauca.saberpro.domain.model.Classification;
import co.edu.unicauca.saberpro.domain.model.Competence;
import co.edu.unicauca.saberpro.domain.model.Context;
import co.edu.unicauca.saberpro.domain.model.Difficulty;
import co.edu.unicauca.saberpro.domain.model.Option;
import co.edu.unicauca.saberpro.domain.model.Question;

import java.util.ArrayList;
import java.util.List;

/** Builder concreto de {@link Question}. Guarda las partes en bruto y las valida todas al construir. */
public class QuestionBuilder implements IQuestionBuilder {

    /** Cantidad de opciones que debe tener toda pregunta del banco. */
    public static final int REQUIRED_OPTIONS = 4;

    private String context;
    private String statement;
    private final List<String> optionTexts = new ArrayList<>();
    private final List<Boolean> optionCorrect = new ArrayList<>();
    private String justification;
    private Competence competence;
    private String topic;
    private String subtopic;
    private Difficulty difficulty;

    @Override
    public IQuestionBuilder setContext(String text) {
        this.context = text;
        return this;
    }

    @Override
    public IQuestionBuilder setStatement(String statement) {
        this.statement = statement;
        return this;
    }

    @Override
    public IQuestionBuilder addOption(String text, boolean correct) {
        optionTexts.add(text);
        optionCorrect.add(correct);
        return this;
    }

    @Override
    public IQuestionBuilder setJustification(String justification) {
        this.justification = justification;
        return this;
    }

    @Override
    public IQuestionBuilder setClassification(Competence competence, String topic, String subtopic,
                                              Difficulty difficulty) {
        this.competence = competence;
        this.topic = topic;
        this.subtopic = subtopic;
        this.difficulty = difficulty;
        return this;
    }

    @Override
    public Question build() {
        List<String> problems = new ArrayList<>();
        if (isBlank(context)) {
            problems.add("falta el contexto");
        }
        if (isBlank(statement)) {
            problems.add("falta el enunciado");
        }
        if (optionTexts.size() != REQUIRED_OPTIONS) {
            problems.add("debe tener exactamente " + REQUIRED_OPTIONS + " opciones y tiene " + optionTexts.size());
        }
        if (optionTexts.stream().anyMatch(this::isBlank)) {
            problems.add("ninguna opción puede estar vacía");
        }
        long correct = optionCorrect.stream().filter(Boolean::booleanValue).count();
        if (correct != 1) {
            problems.add("debe tener exactamente 1 respuesta correcta y tiene " + correct);
        }
        if (isBlank(justification)) {
            problems.add("falta la justificación");
        }
        if (competence == null || difficulty == null || isBlank(topic) || isBlank(subtopic)) {
            problems.add("falta la clasificación (competencia, tema, subtema y dificultad)");
        }
        if (!problems.isEmpty()) {
            throw new InvalidQuestionException(problems);
        }

        List<Option> options = new ArrayList<>();
        for (int i = 0; i < optionTexts.size(); i++) {
            options.add(new Option((char) ('A' + i), optionTexts.get(i), optionCorrect.get(i)));
        }
        Question question = new Question(new Context(context), statement.strip(), options, justification.strip(),
                new Classification(competence, topic, subtopic, difficulty));
        reset();
        return question;
    }

    @Override
    public IQuestionBuilder reset() {
        context = null;
        statement = null;
        optionTexts.clear();
        optionCorrect.clear();
        justification = null;
        competence = null;
        topic = null;
        subtopic = null;
        difficulty = null;
        return this;
    }

    private boolean isBlank(String text) {
        return text == null || text.isBlank();
    }
}
