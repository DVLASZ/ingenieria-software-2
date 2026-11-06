package co.edu.unicauca.saberpro.question.api;

import co.edu.unicauca.saberpro.domain.exception.InvalidQuestionException;
import co.edu.unicauca.saberpro.domain.exception.InvalidTransitionException;
import co.edu.unicauca.saberpro.question.api.dto.ErrorResponse;
import co.edu.unicauca.saberpro.question.application.exception.MessagingUnavailableException;
import co.edu.unicauca.saberpro.question.application.exception.QuestionNotFoundException;
import co.edu.unicauca.saberpro.question.application.exception.QuestionRejectedException;
import co.edu.unicauca.saberpro.question.application.exception.ValidationServiceUnavailableException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(QuestionRejectedException.class)
    ResponseEntity<ErrorResponse> rejected(QuestionRejectedException e) {
        return body(HttpStatus.BAD_REQUEST, "La validación rechazó la pregunta", List.of(e.getMessage()));
    }

    @ExceptionHandler(InvalidQuestionException.class)
    ResponseEntity<ErrorResponse> invalid(InvalidQuestionException e) {
        return body(HttpStatus.BAD_REQUEST, "La pregunta está mal armada", e.getErrors());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ErrorResponse> unreadable(HttpMessageNotReadableException e) {
        return body(HttpStatus.BAD_REQUEST, "El cuerpo de la petición no es un JSON válido", List.of());
    }

    @ExceptionHandler(InvalidTransitionException.class)
    ResponseEntity<ErrorResponse> conflict(InvalidTransitionException e) {
        return body(HttpStatus.CONFLICT, e.getMessage(), List.of());
    }

    @ExceptionHandler(QuestionNotFoundException.class)
    ResponseEntity<ErrorResponse> notFound(QuestionNotFoundException e) {
        return body(HttpStatus.NOT_FOUND, e.getMessage(), List.of());
    }

    @ExceptionHandler({ValidationServiceUnavailableException.class, MessagingUnavailableException.class})
    ResponseEntity<ErrorResponse> unavailable(RuntimeException e) {
        return body(HttpStatus.SERVICE_UNAVAILABLE, e.getMessage(), List.of());
    }

    private ResponseEntity<ErrorResponse> body(HttpStatus status, String error, List<String> details) {
        return ResponseEntity.status(status).body(new ErrorResponse(error, details));
    }
}
