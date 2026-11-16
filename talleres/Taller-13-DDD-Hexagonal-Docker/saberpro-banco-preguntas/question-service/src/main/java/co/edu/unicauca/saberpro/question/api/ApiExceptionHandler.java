package co.edu.unicauca.saberpro.question.api;

import co.edu.unicauca.saberpro.question.domain.exception.PreguntaNoEncontradaException;
import co.edu.unicauca.saberpro.question.domain.exception.ReglaNegocioException;
import co.edu.unicauca.saberpro.question.domain.exception.ServicioNoDisponibleException;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ReglaNegocioException.class)
    ResponseEntity<Map<String, String>> reglaIncumplida(ReglaNegocioException e) {
        return respuesta(HttpStatus.BAD_REQUEST, e);
    }

    @ExceptionHandler(PreguntaNoEncontradaException.class)
    ResponseEntity<Map<String, String>> noExiste(PreguntaNoEncontradaException e) {
        return respuesta(HttpStatus.NOT_FOUND, e);
    }

    @ExceptionHandler(ServicioNoDisponibleException.class)
    ResponseEntity<Map<String, String>> noDisponible(ServicioNoDisponibleException e) {
        return respuesta(HttpStatus.SERVICE_UNAVAILABLE, e);
    }

    private ResponseEntity<Map<String, String>> respuesta(HttpStatus estado, RuntimeException e) {
        return ResponseEntity.status(estado).body(Map.of("mensaje", e.getMessage()));
    }
}
