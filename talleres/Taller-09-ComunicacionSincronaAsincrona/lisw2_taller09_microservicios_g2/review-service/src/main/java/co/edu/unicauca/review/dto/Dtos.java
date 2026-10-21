package co.edu.unicauca.review.dto;

import co.edu.unicauca.review.model.Asignacion;
import co.edu.unicauca.review.model.CasoDeRevision;
import co.edu.unicauca.review.model.CasoEstado;
import co.edu.unicauca.review.model.Decision;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.List;

/** Contratos REST del servicio de revisión. */
public final class Dtos {

    private Dtos() {
    }

    public record AsignarRequest(@NotNull Long questionId, @NotNull List<Long> reviewerIds, @NotNull Long assignedBy) {
    }

    public record EvaluacionRequest(@NotNull Long reviewerId, @NotNull Decision decision, String observation) {
    }

    public record AsignacionDto(Long reviewerId, Decision decision, String observacion, Instant evaluadaEn) {
        public static AsignacionDto de(Asignacion a) {
            return new AsignacionDto(a.getReviewerId(), a.getDecision(), a.getObservacion(), a.getEvaluadaEn());
        }
    }

    public record CasoDto(Long id, Long questionId, int ronda, Long autorId, String titulo, CasoEstado estado, String resultado,
                          Long asignadoPor, List<AsignacionDto> asignaciones, Instant creadoEn, Instant cerradoEn) {
        public static CasoDto de(CasoDeRevision c) {
            return new CasoDto(c.getId(), c.getQuestionId(), c.getRonda(), c.getAutorId(), c.getTitulo(), c.getEstado(),
                    c.getResultado(), c.getAsignadoPor(), c.getAsignaciones().stream().map(AsignacionDto::de).toList(),
                    c.getCreadoEn(), c.getCerradoEn());
        }
    }

    public record PendienteDto(Long questionId, int ronda, String titulo, Long autorId) {
        public static PendienteDto de(CasoDeRevision c) {
            return new PendienteDto(c.getQuestionId(), c.getRonda(), c.getTitulo(), c.getAutorId());
        }
    }

    public record UsuarioInfo(Long id, java.util.Set<String> roles) {
    }
}
