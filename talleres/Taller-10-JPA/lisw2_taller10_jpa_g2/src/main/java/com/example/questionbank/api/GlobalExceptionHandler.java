package com.example.questionbank.api;

import com.example.questionbank.domain.exception.BusinessRuleException;
import com.example.questionbank.domain.exception.NotFoundException;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Turns exceptions into HTTP answers: 404 for missing things, 409 for broken rules, 400 for bad input.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** Creates the handler; Spring registers it as a controller advice. */
    public GlobalExceptionHandler() {
    }

    /**
     * A thing the request refers to does not exist.
     *
     * @param exception the exception
     * @return a 404 problem
     */
    @ExceptionHandler(NotFoundException.class)
    public ProblemDetail notFound(NotFoundException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    /**
     * A business rule does not allow the operation.
     *
     * @param exception the exception
     * @return a 409 problem
     */
    @ExceptionHandler(BusinessRuleException.class)
    public ProblemDetail businessRule(BusinessRuleException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
    }

    /**
     * The database refused the data, for example a repeated username.
     *
     * @param exception the exception
     * @return a 409 problem
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail integrity(DataIntegrityViolationException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                "The data repeats a unique value or points to something that does not exist");
    }

    /**
     * Someone else changed the same question or quiz at the same time.
     *
     * @param exception the exception
     * @return a 409 problem
     */
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ProblemDetail concurrentUpdate(ObjectOptimisticLockingFailureException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                "Someone else changed this at the same time; load it again and retry");
    }

    /**
     * The body failed validation.
     *
     * @param exception the exception
     * @return a 400 problem with the invalid fields
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail invalidBody(MethodArgumentNotValidException exception) {
        Map<String, String> fields = exception.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(error -> error.getField(),
                        error -> String.valueOf(error.getDefaultMessage()), (first, second) -> first));
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "The request is not valid");
        problem.setProperty("fields", fields);
        return problem;
    }

    /**
     * The body or a parameter could not be read, for example an unknown enum value.
     *
     * @param exception the exception
     * @return a 400 problem
     */
    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ProblemDetail unreadable(Exception exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "The request could not be read");
    }
}
