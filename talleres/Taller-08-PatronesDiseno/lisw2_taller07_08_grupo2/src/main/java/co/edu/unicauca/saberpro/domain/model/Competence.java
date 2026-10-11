package co.edu.unicauca.saberpro.domain.model;

/** Competencias de las pruebas Saber Pro (las mismas del banco de preguntas del proyecto de curso). */
public enum Competence {
    CRITICAL_READING("Lectura crítica"),
    QUANTITATIVE_REASONING("Razonamiento cuantitativo"),
    CITIZENSHIP("Competencias ciudadanas"),
    WRITTEN_COMMUNICATION("Comunicación escrita"),
    ENGLISH("Inglés");

    private final String label;

    Competence(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
