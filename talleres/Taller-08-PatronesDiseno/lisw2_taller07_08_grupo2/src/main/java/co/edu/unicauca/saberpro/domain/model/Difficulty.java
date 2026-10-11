package co.edu.unicauca.saberpro.domain.model;

/** Nivel de dificultad de una pregunta. */
public enum Difficulty {
    BASIC("Básico"),
    INTERMEDIATE("Intermedio"),
    ADVANCED("Avanzado");

    private final String label;

    Difficulty(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
