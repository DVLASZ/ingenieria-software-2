package co.edu.unicauca.saberpro.domain.model;

/**
 * Clasificación de una pregunta dentro del banco.
 *
 * @param competence competencia evaluada
 * @param topic      tema
 * @param subtopic   subtema
 * @param difficulty nivel de dificultad
 */
public record Classification(Competence competence, String topic, String subtopic, Difficulty difficulty) {

    public Classification {
        if (competence == null || difficulty == null) {
            throw new IllegalArgumentException("La competencia y la dificultad son obligatorias");
        }
        if (topic == null || topic.isBlank() || subtopic == null || subtopic.isBlank()) {
            throw new IllegalArgumentException("El tema y el subtema son obligatorios");
        }
        topic = topic.strip();
        subtopic = subtopic.strip();
    }
}
