package co.edu.unicauca.lifecycle.controller;

import co.edu.unicauca.lifecycle.dto.Dtos.CrearEstadoRequest;
import co.edu.unicauca.lifecycle.dto.Dtos.EstadoDto;
import co.edu.unicauca.lifecycle.dto.Dtos.OperacionDto;
import co.edu.unicauca.lifecycle.dto.Dtos.TransicionGlobalRequest;
import co.edu.unicauca.lifecycle.dto.Dtos.TransicionRequest;
import co.edu.unicauca.lifecycle.model.QuestionState;
import co.edu.unicauca.lifecycle.service.LifecycleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/lifecycle")
public class LifecycleController {

    private final LifecycleService servicio;

    public LifecycleController(LifecycleService servicio) {
        this.servicio = servicio;
    }

    /** Lo llama question-service al crear una pregunta: nace en Borrador. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EstadoDto crear(@Valid @RequestBody CrearEstadoRequest request) {
        QuestionState estado = servicio.crear(request.questionId(), request.autorId(), request.titulo(), request.competencia());
        return vista(estado);
    }

    @GetMapping("/{questionId}")
    public EstadoDto obtener(@PathVariable Long questionId) {
        return vista(servicio.obtener(questionId));
    }

    /** Transición síncrona pedida por otro servicio (por ejemplo, review-service al asignar o decidir). */
    @PostMapping("/{questionId}/transition")
    public EstadoDto transicionar(@PathVariable Long questionId, @Valid @RequestBody TransicionRequest request) {
        return vista(servicio.transicionar(questionId, request.to(), request.requestedBy(), request.motivo()));
    }

    /** Misma transición, por la ruta que usa la guía del taller. */
    @PostMapping("/transition")
    public EstadoDto transicionarPorCuerpo(@Valid @RequestBody TransicionGlobalRequest request) {
        return vista(servicio.transicionar(request.questionId(), request.to(), request.requestedBy(), request.motivo()));
    }

    /** Resultado de una operación asíncrona (COMPLETED o FAILED con el motivo). */
    @GetMapping("/operations/{operationId}")
    public OperacionDto operacion(@PathVariable String operationId) {
        return OperacionDto.de(servicio.operacion(operationId));
    }

    private EstadoDto vista(QuestionState estado) {
        return EstadoDto.de(estado, servicio.historialDe(estado.getQuestionId()));
    }
}
