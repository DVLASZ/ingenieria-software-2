package co.edu.unicauca.question.controller;

import co.edu.unicauca.question.dto.Dtos.AceptadaDto;
import co.edu.unicauca.question.dto.Dtos.PaginaDto;
import co.edu.unicauca.question.dto.Dtos.PreguntaDto;
import co.edu.unicauca.question.dto.Dtos.PreguntaRequest;
import co.edu.unicauca.question.dto.Dtos.SolicitudRequest;
import co.edu.unicauca.question.model.Comando;
import co.edu.unicauca.question.model.EstadoPregunta;
import co.edu.unicauca.question.model.Operation;
import co.edu.unicauca.question.service.QuestionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/questions")
public class QuestionController {

    private final QuestionService servicio;

    public QuestionController(QuestionService servicio) {
        this.servicio = servicio;
    }

    /** Creación síncrona: 201 si la validación pasa, 400 con los errores si no. */
    @PostMapping
    public ResponseEntity<PreguntaDto> crear(@RequestHeader(value = "Authorization", required = false) String autorizacion,
                                             @Valid @RequestBody PreguntaRequest request) {
        exigirAutorizacion(autorizacion);
        return ResponseEntity.status(HttpStatus.CREATED).body(PreguntaDto.de(servicio.crear(request)));
    }

    @GetMapping
    public PaginaDto<PreguntaDto> listar(@RequestParam(required = false) EstadoPregunta estado,
                                         @RequestParam(required = false) String competencia,
                                         @RequestParam(required = false) String tema,
                                         @RequestParam(required = false) Long autorId,
                                         @RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "10") int size) {
        return PaginaDto.de(servicio.listar(estado, competencia, tema, autorId, page, size), PreguntaDto::de);
    }

    @GetMapping("/{id}")
    public PreguntaDto obtener(@PathVariable Long id) {
        return PreguntaDto.de(servicio.obtener(id));
    }

    /** Edición: solo en Borrador o Rechazada (esta última vuelve a Borrador). */
    @PutMapping("/{id}")
    public PreguntaDto actualizar(@RequestHeader(value = "Authorization", required = false) String autorizacion,
                                  @PathVariable Long id, @Valid @RequestBody PreguntaRequest request) {
        exigirAutorizacion(autorizacion);
        return PreguntaDto.de(servicio.actualizar(id, request));
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<AceptadaDto> enviarARevision(@RequestHeader(value = "Authorization", required = false) String autorizacion,
                                                       @PathVariable Long id,
                                                       @RequestBody(required = false) SolicitudRequest solicitud) {
        return aceptar(autorizacion, id, Comando.ENVIAR_A_REVISION, solicitud, "Solicitud de envío a revisión aceptada para procesamiento");
    }

    @PostMapping("/{id}/publish")
    public ResponseEntity<AceptadaDto> publicar(@RequestHeader(value = "Authorization", required = false) String autorizacion,
                                                @PathVariable Long id,
                                                @RequestBody(required = false) SolicitudRequest solicitud) {
        return aceptar(autorizacion, id, Comando.PUBLICAR, solicitud, "Solicitud de publicación aceptada para procesamiento");
    }

    @PostMapping("/{id}/archive")
    public ResponseEntity<AceptadaDto> archivar(@RequestHeader(value = "Authorization", required = false) String autorizacion,
                                                @PathVariable Long id,
                                                @RequestBody(required = false) SolicitudRequest solicitud) {
        return aceptar(autorizacion, id, Comando.ARCHIVAR, solicitud, "Solicitud de archivo aceptada para procesamiento");
    }

    /** 202 Accepted: confirma la recepción, pero no que la transición haya terminado. */
    private ResponseEntity<AceptadaDto> aceptar(String autorizacion, Long id, Comando comando, SolicitudRequest solicitud, String mensaje) {
        exigirAutorizacion(autorizacion);
        Operation operacion = servicio.solicitar(id, comando,
                solicitud == null ? null : solicitud.requestedBy(), solicitud == null ? null : solicitud.reason());
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(new AceptadaDto(mensaje, operacion.getOperationId(), "PROCESSING"));
    }

    /** Sin JWT en esta versión: basta con que el encabezado exista y no esté vacío. */
    private static void exigirAutorizacion(String autorizacion) {
        if (autorizacion == null || autorizacion.isBlank()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Falta el encabezado Authorization");
        }
    }
}
