package co.edu.unicauca.validation.controller;

import co.edu.unicauca.validation.dto.Dtos.ValidationRequest;
import co.edu.unicauca.validation.dto.Dtos.ValidationResult;
import co.edu.unicauca.validation.messaging.ValidationEventPublisher;
import co.edu.unicauca.validation.service.QuestionValidator;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ValidationController {

    private final QuestionValidator validador;
    private final ValidationEventPublisher eventos;

    public ValidationController(QuestionValidator validador, ValidationEventPublisher eventos) {
        this.validador = validador;
        this.eventos = eventos;
    }

    /** Siempre responde 200: el resultado dice si es válida; quien llama decide el código HTTP que devuelve. */
    @PostMapping("/validate")
    public ValidationResult validar(@RequestBody ValidationRequest request) {
        List<String> errores = validador.validar(request);
        if (!errores.isEmpty()) {
            eventos.validacionFallida(request.autorId(), request.titulo(), errores);
        }
        return new ValidationResult(errores.isEmpty(), errores);
    }
}
