package co.edu.unicauca.review.service;

import co.edu.unicauca.review.client.LifecycleClient;
import co.edu.unicauca.review.client.UserClient;
import co.edu.unicauca.review.controller.ApiException;
import co.edu.unicauca.review.dto.Dtos.UsuarioInfo;
import co.edu.unicauca.review.messaging.ReviewEventPublisher;
import co.edu.unicauca.review.model.Asignacion;
import co.edu.unicauca.review.model.CasoDeRevision;
import co.edu.unicauca.review.model.CasoEstado;
import co.edu.unicauca.review.model.Decision;
import co.edu.unicauca.review.repository.CasoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

/**
 * Reglas de revisión del proyecto: de 1 a 3 revisores, el autor nunca revisa su pregunta, unanimidad para aprobar y un solo
 * rechazo cierra la ronda. Los cambios de estado los hace lifecycle-service; aquí solo se piden.
 */
@Service
public class ReviewService {

    static final int MINIMO_REVISORES = 1;
    static final int MAXIMO_REVISORES = 3;

    private final CasoRepository casos;
    private final LifecycleClient ciclo;
    private final UserClient usuarios;
    private final ReviewEventPublisher eventos;

    public ReviewService(CasoRepository casos, LifecycleClient ciclo, UserClient usuarios, ReviewEventPublisher eventos) {
        this.casos = casos;
        this.ciclo = ciclo;
        this.usuarios = usuarios;
        this.eventos = eventos;
    }

    /** Abre la siguiente ronda de revisión de una pregunta enviada. Un evento repetido no abre otra. */
    @Transactional
    public void abrirCaso(String eventId, Long questionId, Long autorId, String titulo) {
        if (questionId == null || (eventId != null && casos.existsByEventId(eventId))) {
            return;
        }
        int ronda = casos.findFirstByQuestionIdOrderByRondaDesc(questionId).map(c -> c.getRonda() + 1).orElse(1);
        casos.save(new CasoDeRevision(questionId, ronda, autorId, titulo, eventId));
    }

    /**
     * Asigna de 1 a 3 revisores. Se guardan las asignaciones y luego se pide la transición a lifecycle: si esta falla, la
     * transacción se revierte y no queda ninguna asignación sin su cambio de estado.
     */
    @Transactional
    public CasoDeRevision asignar(Long questionId, List<Long> revisores, Long administrador) {
        if (revisores.size() < MINIMO_REVISORES || revisores.size() > MAXIMO_REVISORES) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Se deben asignar entre " + MINIMO_REVISORES + " y " + MAXIMO_REVISORES + " revisores");
        }
        if (new HashSet<>(revisores).size() != revisores.size()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "No se puede repetir un revisor");
        }
        CasoDeRevision caso = casos.findFirstByQuestionIdAndEstadoOrderByRondaDesc(questionId, CasoEstado.PENDIENTE_ASIGNACION)
                .orElseThrow(() -> casos.findFirstByQuestionIdOrderByRondaDesc(questionId).isPresent()
                        ? new ApiException(HttpStatus.CONFLICT, "La pregunta " + questionId + " no está pendiente de asignación")
                        : new ApiException(HttpStatus.NOT_FOUND, "La pregunta " + questionId + " no ha sido enviada a revisión"));

        UsuarioInfo admin = usuarios.buscar(administrador)
                .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, "El usuario " + administrador + " no existe"));
        if (!admin.roles().contains("ADMINISTRADOR")) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Solo un administrador puede asignar revisores");
        }
        for (Long revisor : revisores) {
            if (revisor.equals(caso.getAutorId())) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "El autor no puede ser revisor de su propia pregunta");
            }
            Optional<UsuarioInfo> info = usuarios.buscar(revisor);
            if (info.isEmpty() || !info.get().roles().contains("REVISOR")) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "El usuario " + revisor + " no es un revisor elegible");
            }
        }
        String estado = ciclo.estadoDe(questionId);
        if (!"PENDIENTE_REVISION".equals(estado)) {
            throw new ApiException(HttpStatus.CONFLICT, "La pregunta no es elegible: está en " + estado + " y debe estar en PENDIENTE_REVISION");
        }

        caso.asignar(revisores, administrador);
        casos.saveAndFlush(caso);
        ciclo.transicionar(questionId, "EN_REVISION", String.valueOf(administrador), null);
        eventos.revisoresAsignados(caso, revisores);
        return caso;
    }

    /**
     * Registra la evaluación de un revisor. Un rechazo cierra la ronda como RECHAZADA; si todos aprueban, queda APROBADA
     * (unanimidad). Si lifecycle no acepta la transición, la evaluación no se guarda.
     */
    @Transactional
    public CasoDeRevision evaluar(Long questionId, Long revisor, Decision decision, String observacion) {
        CasoDeRevision caso = casos.findFirstByQuestionIdAndEstadoOrderByRondaDesc(questionId, CasoEstado.EN_CURSO)
                .orElseThrow(() -> new ApiException(HttpStatus.CONFLICT, "La pregunta " + questionId + " no tiene una revisión en curso"));
        Asignacion asignacion = caso.getAsignaciones().stream().filter(a -> a.getReviewerId().equals(revisor)).findFirst()
                .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, "El usuario " + revisor + " no está asignado a esta pregunta"));
        if (asignacion.getDecision() != null) {
            throw new ApiException(HttpStatus.CONFLICT, "El revisor " + revisor + " ya evaluó esta pregunta");
        }
        if (decision == Decision.REJECT && (observacion == null || observacion.isBlank())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Rechazar exige una observación");
        }

        asignacion.evaluar(decision, observacion);
        eventos.evaluacionEnviada(caso, revisor, decision.name(), observacion);

        if (decision == Decision.REJECT) {
            ciclo.transicionar(questionId, "RECHAZADA", String.valueOf(revisor), observacionesDe(caso).stream().reduce((a, b) -> a + " | " + b).orElse(observacion));
            caso.cerrar("REJECTED");
            eventos.revisionCompletada(caso, "REJECTED", observacionesDe(caso));
        } else if (caso.todasEvaluadas()) {
            ciclo.transicionar(questionId, "APROBADA", String.valueOf(revisor), null);
            caso.cerrar("APPROVED");
            eventos.revisionCompletada(caso, "APPROVED", observacionesDe(caso));
        }
        return casos.save(caso);
    }

    public List<CasoDeRevision> rondasDe(Long questionId) {
        List<CasoDeRevision> rondas = casos.findByQuestionIdOrderByRondaAsc(questionId);
        if (rondas.isEmpty()) {
            throw new ApiException(HttpStatus.NOT_FOUND, "La pregunta " + questionId + " no tiene revisiones");
        }
        return rondas;
    }

    public List<CasoDeRevision> pendientesDe(Long revisor) {
        return casos.pendientesDe(revisor);
    }

    private static List<String> observacionesDe(CasoDeRevision caso) {
        return caso.getAsignaciones().stream()
                .filter(a -> a.getObservacion() != null && !a.getObservacion().isBlank())
                .map(a -> "Revisor " + a.getReviewerId() + ": " + a.getObservacion()).toList();
    }
}
