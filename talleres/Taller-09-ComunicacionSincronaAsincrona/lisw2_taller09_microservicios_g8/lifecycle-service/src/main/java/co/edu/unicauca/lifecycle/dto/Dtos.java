package co.edu.unicauca.lifecycle.dto;

import co.edu.unicauca.lifecycle.model.EstadoPregunta;
import co.edu.unicauca.lifecycle.model.OperationResult;
import co.edu.unicauca.lifecycle.model.QuestionState;
import co.edu.unicauca.lifecycle.model.StateTransition;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.List;

/** Contratos REST del servicio de ciclo de vida. */
public final class Dtos {

    private Dtos() {
    }

    public record CrearEstadoRequest(@NotNull Long questionId, @NotNull Long autorId, String titulo, String competencia) {
    }

    public record TransicionRequest(@NotNull EstadoPregunta to, String requestedBy, String motivo) {
    }

    /** Variante de la guía del taller: `POST /lifecycle/transition`, con la pregunta en el cuerpo. */
    public record TransicionGlobalRequest(@NotNull Long questionId, @NotNull EstadoPregunta to, String requestedBy, String motivo) {
    }

    public record TransicionDto(EstadoPregunta desde, EstadoPregunta hasta, String requestedBy, String motivo, Instant ocurridaEn) {
        public static TransicionDto de(StateTransition t) {
            return new TransicionDto(t.getDesde(), t.getHasta(), t.getRequestedBy(), t.getMotivo(), t.getOcurridaEn());
        }
    }

    /** Estado de una pregunta con su nombre en español, su etiqueta y su equivalente en inglés. */
    public record EstadoDto(Long questionId, EstadoPregunta estado, String etiqueta, String equivalente, Long autorId,
                            String titulo, String competencia, String changedBy, Instant changedAt, List<TransicionDto> historial) {
        public static EstadoDto de(QuestionState s, List<StateTransition> historial) {
            return new EstadoDto(s.getQuestionId(), s.getEstado(), s.getEstado().etiqueta(), s.getEstado().equivalente(),
                    s.getAutorId(), s.getTitulo(), s.getCompetencia(), s.getChangedBy(), s.getChangedAt(),
                    historial.stream().map(TransicionDto::de).toList());
        }
    }

    public record OperacionDto(String operationId, Long questionId, String tipo, String status, String mensaje, Instant completedAt) {
        public static OperacionDto de(OperationResult r) {
            return new OperacionDto(r.getOperationId(), r.getQuestionId(), r.getTipo(), r.getStatus().name(), r.getMensaje(), r.getCompletedAt());
        }
    }
}
