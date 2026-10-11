package co.edu.unicauca.saberpro.domain.model;

/**
 * Contexto de una pregunta: el texto, caso o situación que la enmarca.
 *
 * @param text texto del contexto; no puede estar vacío
 */
public record Context(String text) {

    public Context {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("El contexto no puede estar vacío");
        }
        text = text.strip();
    }
}
