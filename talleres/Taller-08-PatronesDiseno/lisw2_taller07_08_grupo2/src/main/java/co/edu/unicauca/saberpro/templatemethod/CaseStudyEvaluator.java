package co.edu.unicauca.saberpro.templatemethod;

import java.util.List;
import java.util.Optional;

/**
 * Evalúa una pregunta de caso de estudio: la respuesta es un texto libre y el puntaje es la fracción de los criterios
 * de la rúbrica (palabras clave) que el texto menciona. Recibe sus propios datos, igual que el de verdadero/falso.
 */
public class CaseStudyEvaluator extends QuestionEvaluator {

    /** Longitud mínima para que un texto cuente como respuesta de caso de estudio. */
    public static final int MIN_LENGTH = 30;

    private final List<String> criteria;

    /** @param criteria palabras o frases que debe mencionar una buena respuesta; no puede estar vacía */
    public CaseStudyEvaluator(List<String> criteria) {
        if (criteria == null || criteria.isEmpty()) {
            throw new IllegalArgumentException("La rúbrica debe tener al menos un criterio");
        }
        this.criteria = criteria.stream().map(String::toLowerCase).toList();
    }

    @Override
    protected Optional<String> validateFormat(String answer) {
        return answer.length() >= MIN_LENGTH ? Optional.empty()
                : Optional.of("La respuesta debe tener al menos " + MIN_LENGTH + " caracteres y tiene "
                        + answer.length());
    }

    @Override
    protected double calculateScore(String answer) {
        return (double) mentioned(answer).size() / criteria.size() * FULL_SCORE;
    }

    @Override
    protected String generateFeedback(String answer, double score) {
        List<String> missing = criteria.stream().filter(criterion -> !mentioned(answer).contains(criterion)).toList();
        return missing.isEmpty()
                ? "¡Excelente! La respuesta cubre todos los criterios de la rúbrica."
                : "La respuesta cubre " + mentioned(answer).size() + " de " + criteria.size()
                        + " criterios. Falta mencionar: " + String.join(", ", missing) + ".";
    }

    private List<String> mentioned(String answer) {
        String text = answer.toLowerCase();
        return criteria.stream().filter(text::contains).toList();
    }
}
