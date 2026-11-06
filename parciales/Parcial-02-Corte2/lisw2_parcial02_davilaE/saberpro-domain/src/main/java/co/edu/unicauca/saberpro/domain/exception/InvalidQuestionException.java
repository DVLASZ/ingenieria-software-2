package co.edu.unicauca.saberpro.domain.exception;

import java.util.List;

public class InvalidQuestionException extends RuntimeException {

    private final List<String> errors;

    public InvalidQuestionException(List<String> errors) {
        super("Pregunta mal armada: " + String.join("; ", errors));
        this.errors = List.copyOf(errors);
    }

    public List<String> getErrors() {
        return errors;
    }
}
