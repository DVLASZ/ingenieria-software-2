package co.edu.unicauca.saberpro.question.application.port;

/** puerto hacia validation-service */
public interface IValidationClient {

    ValidationResult validate(String justification);

    record ValidationResult(boolean valid, String message) {
    }
}
