package co.edu.unicauca.saberpro.question.application.exception;

/** validation-service dijo que la justificación no sirve */
public class QuestionRejectedException extends RuntimeException {

    public QuestionRejectedException(String message) {
        super(message);
    }
}
