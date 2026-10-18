package co.edu.unicauca.question.controller;

import co.edu.unicauca.question.dto.Dtos.OperacionDto;
import co.edu.unicauca.question.service.QuestionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/operations")
public class OperationController {

    private final QuestionService servicio;

    public OperationController(QuestionService servicio) {
        this.servicio = servicio;
    }

    /** PENDING mientras lifecycle no responde; luego COMPLETED o FAILED con el motivo. */
    @GetMapping("/{operationId}")
    public OperacionDto consultar(@PathVariable String operationId) {
        return OperacionDto.de(servicio.operacion(operationId));
    }
}
