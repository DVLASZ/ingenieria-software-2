package co.edu.unicauca.saberpro.builder;

import java.util.List;

/** Se lanza cuando se intenta construir una pregunta incompleta o que incumple las reglas básicas. */
public class InvalidQuestionException extends RuntimeException {

    private final List<String> problems;

    public InvalidQuestionException(List<String> problems) {
        super("Pregunta inválida: " + String.join("; ", problems));
        this.problems = List.copyOf(problems);
    }

    /** Cada incumplimiento detectado, para mostrarlos todos al usuario de una vez. */
    public List<String> getProblems() {
        return problems;
    }
}
