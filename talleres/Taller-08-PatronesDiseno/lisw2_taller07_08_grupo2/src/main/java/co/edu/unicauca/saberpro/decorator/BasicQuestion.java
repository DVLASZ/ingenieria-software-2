package co.edu.unicauca.saberpro.decorator;

import co.edu.unicauca.saberpro.domain.model.Question;

/** Componente concreto: una pregunta sin características añadidas, con puntaje base de 1.0. */
public class BasicQuestion implements QuestionComponent {

    /** Puntaje base de toda pregunta. */
    public static final double BASE_SCORE = 1.0;

    private final Question question;

    public BasicQuestion(Question question) {
        this.question = question;
    }

    @Override
    public Question getQuestion() {
        return question;
    }

    @Override
    public double getMaxScore() {
        return BASE_SCORE;
    }

    @Override
    public String describe() {
        return question.getStatement();
    }

    @Override
    public String getExplanation() {
        return question.getJustification();
    }
}
