package co.edu.unicauca.saberpro.templatemethod;

/**
 * Resultado de evaluar una respuesta.
 *
 * @param valid    si la respuesta tenía el formato esperado
 * @param score    puntaje entre 0.0 (nada) y 1.0 (puntaje completo)
 * @param feedback retroalimentación para quien respondió
 */
public record EvaluationResult(boolean valid, double score, String feedback) {

    /** Resultado de una respuesta con formato inválido: puntaje cero y el motivo como retroalimentación. */
    public static EvaluationResult invalid(String reason) {
        return new EvaluationResult(false, 0.0, reason);
    }
}
