package co.edu.unicauca.saberpro.templatemethod;

import java.util.Optional;
import java.util.Set;

/**
 * Evalúa una pregunta de verdadero/falso. Recibe sus propios datos (la respuesta esperada y su explicación) porque
 * este tipo de pregunta no pasa por el Builder, que está pensado para selección múltiple.
 */
public class TrueFalseEvaluator extends QuestionEvaluator {

    private static final Set<String> TRUE_WORDS = Set.of("v", "verdadero", "true", "t");
    private static final Set<String> FALSE_WORDS = Set.of("f", "falso", "false");

    private final boolean expected;
    private final String explanation;

    public TrueFalseEvaluator(boolean expected, String explanation) {
        this.expected = expected;
        this.explanation = explanation;
    }

    @Override
    protected String receiveAnswer(String rawAnswer) {
        return super.receiveAnswer(rawAnswer).toLowerCase();
    }

    @Override
    protected Optional<String> validateFormat(String answer) {
        boolean known = TRUE_WORDS.contains(answer) || FALSE_WORDS.contains(answer);
        return known ? Optional.empty()
                : Optional.of("La respuesta debe ser verdadero o falso (V/F) y se recibió '" + answer + "'");
    }

    @Override
    protected double calculateScore(String answer) {
        return TRUE_WORDS.contains(answer) == expected ? FULL_SCORE : 0.0;
    }

    @Override
    protected String generateFeedback(String answer, double score) {
        String verdict = expected ? "verdadera" : "falsa";
        return score == FULL_SCORE
                ? "¡Correcto! La afirmación es " + verdict + ". " + explanation
                : "Incorrecto. La afirmación es " + verdict + ". " + explanation;
    }
}
