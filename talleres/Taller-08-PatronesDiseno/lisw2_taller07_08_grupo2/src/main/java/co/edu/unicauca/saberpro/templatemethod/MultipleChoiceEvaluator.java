package co.edu.unicauca.saberpro.templatemethod;

import co.edu.unicauca.saberpro.domain.model.Option;
import co.edu.unicauca.saberpro.domain.model.Question;

import java.util.Optional;

/** Evalúa una pregunta de selección múltiple: la respuesta es la letra de la opción elegida. */
public class MultipleChoiceEvaluator extends QuestionEvaluator {

    private final Question question;

    public MultipleChoiceEvaluator(Question question) {
        this.question = question;
    }

    @Override
    protected String receiveAnswer(String rawAnswer) {
        return super.receiveAnswer(rawAnswer).toUpperCase();
    }

    @Override
    protected Optional<String> validateFormat(String answer) {
        boolean isOptionLetter = question.getOptions().stream()
                .anyMatch(option -> String.valueOf(option.label()).equals(answer));
        return isOptionLetter ? Optional.empty()
                : Optional.of("La respuesta debe ser la letra de una opción (A a D) y se recibió '" + answer + "'");
    }

    @Override
    protected double calculateScore(String answer) {
        return answer.charAt(0) == question.getCorrectOption().label() ? FULL_SCORE : 0.0;
    }

    @Override
    protected String generateFeedback(String answer, double score) {
        Option correct = question.getCorrectOption();
        return score == FULL_SCORE
                ? "¡Correcto! " + question.getJustification()
                : "Incorrecto. La respuesta correcta es " + correct.label() + ") " + correct.text() + ". "
                        + question.getJustification();
    }
}
