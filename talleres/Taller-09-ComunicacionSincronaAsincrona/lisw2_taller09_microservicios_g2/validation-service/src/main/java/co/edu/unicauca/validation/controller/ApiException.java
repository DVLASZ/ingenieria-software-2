package co.edu.unicauca.validation.controller;

import org.springframework.http.HttpStatus;

import java.util.List;

/** Error de negocio con el código HTTP que debe recibir el cliente. */
public class ApiException extends RuntimeException {

    private final HttpStatus status;
    private final List<String> errors;

    public ApiException(HttpStatus status, String message) {
        this(status, message, List.of());
    }

    public ApiException(HttpStatus status, String message, List<String> errors) {
        super(message);
        this.status = status;
        this.errors = errors;
    }

    public HttpStatus status() {
        return status;
    }

    public List<String> errors() {
        return errors;
    }
}
