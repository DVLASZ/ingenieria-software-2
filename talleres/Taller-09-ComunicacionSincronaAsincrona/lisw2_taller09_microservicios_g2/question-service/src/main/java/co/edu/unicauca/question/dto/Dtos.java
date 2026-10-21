package co.edu.unicauca.question.dto;

import co.edu.unicauca.question.model.EstadoPregunta;
import co.edu.unicauca.question.model.Operation;
import co.edu.unicauca.question.model.Question;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;

import java.time.Instant;
import java.util.List;

/** Contratos REST del servicio de preguntas. */
public final class Dtos {

    private Dtos() {
    }

    /** Contenido de la pregunta. La estructura (opciones, clave, contexto…) la valida validation-service. */
    public record PreguntaRequest(@NotNull Long autorId, @NotBlank String titulo, String contexto, String pregunta,
                                  @NotNull List<String> opciones, String respuestaCorrecta, String justificacion,
                                  String bibliografia, @NotBlank String competencia, String tema, String subtema,
                                  String dificultad) {
    }

    /** Quién pide una operación asíncrona y, opcionalmente, por qué. */
    public record SolicitudRequest(String requestedBy, String reason) {
    }

    public record PreguntaDto(Long id, Long autorId, String titulo, String contexto, String pregunta, List<String> opciones,
                              String respuestaCorrecta, String justificacion, String bibliografia, String competencia,
                              String tema, String subtema, String dificultad, EstadoPregunta estado, String etiqueta,
                              String equivalente, int version, Instant creadaEn, Instant actualizadaEn) {
        public static PreguntaDto de(Question q) {
            return new PreguntaDto(q.getId(), q.getAutorId(), q.getTitulo(), q.getContexto(), q.getPregunta(), q.getOpciones(),
                    q.getRespuestaCorrecta(), q.getJustificacion(), q.getBibliografia(), q.getCompetencia(), q.getTema(),
                    q.getSubtema(), q.getDificultad(), q.getEstado(), q.getEstado().etiqueta(), q.getEstado().equivalente(),
                    q.getVersion(), q.getCreadaEn(), q.getActualizadaEn());
        }
    }

    public record PaginaDto<T>(List<T> content, int page, int size, long totalElements, int totalPages) {
        public static <E, T> PaginaDto<T> de(Page<E> pagina, java.util.function.Function<E, T> conversor) {
            return new PaginaDto<>(pagina.getContent().stream().map(conversor).toList(), pagina.getNumber(), pagina.getSize(),
                    pagina.getTotalElements(), pagina.getTotalPages());
        }
    }

    /** Respuesta 202: la solicitud fue aceptada, pero la transición todavía no necesariamente terminó. */
    public record AceptadaDto(String message, String operationId, String status) {
    }

    public record OperacionDto(String operationId, Long questionId, String tipo, String status, String mensaje,
                               Instant creadaEn, Instant completadaEn) {
        public static OperacionDto de(Operation o) {
            return new OperacionDto(o.getOperationId(), o.getQuestionId(), o.getTipo(), o.getStatus().name(), o.getMensaje(),
                    o.getCreadaEn(), o.getCompletadaEn());
        }
    }

    public record ResultadoValidacion(boolean valid, List<String> errors) {
    }

    /** Lo que user-service responde de un usuario: su identificador y sus roles. */
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
    public record UsuarioInfo(Long id, java.util.Set<String> roles) {
    }
}
