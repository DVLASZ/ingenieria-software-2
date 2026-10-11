package co.edu.unicauca.saberpro.domain.model;

/**
 * Opción de respuesta de una pregunta de selección múltiple.
 *
 * @param label   letra de la opción (A, B, C o D)
 * @param text    texto de la opción; no puede estar vacío
 * @param correct indica si es la respuesta correcta
 */
public record Option(char label, String text, boolean correct) {

    public Option {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("El texto de la opción " + label + " no puede estar vacío");
        }
        text = text.strip();
    }
}
