package co.edu.unicauca.saberpro.templatemethod;

import java.util.Optional;

/**
 * Patrón Template Method (comportamiento): define el esqueleto del algoritmo de evaluación y deja a las subclases los
 * pasos que cambian según el tipo de pregunta.
 *
 * <p>{@link #evaluate(String)} es la plantilla y es {@code final}: 1) recibir la respuesta, 2) validar el formato,
 * 3) calcular el puntaje específico y 4) generar la retroalimentación. Si el formato no es válido, los pasos 3 y 4 no se
 * ejecutan.
 */
public abstract class QuestionEvaluator {

    /** Puntaje completo; todos los puntajes van de 0.0 a este valor. */
    protected static final double FULL_SCORE = 1.0;

    /** Método plantilla: el orden de los pasos no se puede alterar desde las subclases. */
    public final EvaluationResult evaluate(String rawAnswer) {
        String answer = receiveAnswer(rawAnswer);
        return validateFormat(answer)
                .map(EvaluationResult::invalid)
                .orElseGet(() -> scoreAndGiveFeedback(answer));
    }

    private EvaluationResult scoreAndGiveFeedback(String answer) {
        double score = calculateScore(answer);
        return new EvaluationResult(true, score, generateFeedback(answer, score));
    }

    /** Paso 1: normaliza la respuesta recibida. Las subclases pueden ampliar la normalización. */
    protected String receiveAnswer(String rawAnswer) {
        return rawAnswer == null ? "" : rawAnswer.strip();
    }

    /** Paso 2: devuelve el motivo si el formato es inválido, o vacío si es válido. */
    protected abstract Optional<String> validateFormat(String answer);

    /** Paso 3: puntaje entre 0.0 y {@link #FULL_SCORE}; solo se llama con respuestas de formato válido. */
    protected abstract double calculateScore(String answer);

    /** Paso 4: retroalimentación según la respuesta y el puntaje obtenido. */
    protected abstract String generateFeedback(String answer, double score);
}
