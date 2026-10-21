package co.edu.unicauca.lifecycle.service;

import co.edu.unicauca.lifecycle.controller.ApiException;
import co.edu.unicauca.lifecycle.dto.EventEnvelope;
import co.edu.unicauca.lifecycle.messaging.LifecycleEventPublisher;
import co.edu.unicauca.lifecycle.model.EstadoPregunta;
import co.edu.unicauca.lifecycle.model.OperationResult;
import co.edu.unicauca.lifecycle.model.OperationStatus;
import co.edu.unicauca.lifecycle.model.QuestionState;
import co.edu.unicauca.lifecycle.model.StateTransition;
import co.edu.unicauca.lifecycle.repository.OperationResultRepository;
import co.edu.unicauca.lifecycle.repository.QuestionStateRepository;
import co.edu.unicauca.lifecycle.repository.StateTransitionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/** Reglas del ciclo de vida: aquí, y solo aquí, se cambia el estado de una pregunta. */
@Service
public class LifecycleService {

    /** Comando recibido → estado al que debe pasar la pregunta. */
    private static final Map<String, EstadoPregunta> DESTINO_DEL_COMANDO = Map.of(
            "SubmitQuestionForReview", EstadoPregunta.PENDIENTE_REVISION,
            "PublishQuestion", EstadoPregunta.PUBLICADA,
            "ArchiveQuestion", EstadoPregunta.ARCHIVADA);

    private final QuestionStateRepository estados;
    private final StateTransitionRepository historial;
    private final OperationResultRepository resultados;
    private final LifecycleEventPublisher eventos;

    public LifecycleService(QuestionStateRepository estados, StateTransitionRepository historial,
                            OperationResultRepository resultados, LifecycleEventPublisher eventos) {
        this.estados = estados;
        this.historial = historial;
        this.resultados = resultados;
        this.eventos = eventos;
    }

    public QuestionState crear(Long questionId, Long autorId, String titulo, String competencia) {
        if (estados.existsById(questionId)) {
            throw new ApiException(HttpStatus.CONFLICT, "La pregunta " + questionId + " ya tiene un estado");
        }
        QuestionState estado = estados.save(new QuestionState(questionId, autorId, titulo, competencia));
        historial.save(new StateTransition(questionId, null, EstadoPregunta.BORRADOR, String.valueOf(autorId), "Pregunta creada"));
        eventos.estadoCambiado(questionId, null, EstadoPregunta.BORRADOR, String.valueOf(autorId), "Pregunta creada");
        return estado;
    }

    public QuestionState transicionar(Long questionId, EstadoPregunta hasta, String solicitadoPor, String motivo) {
        return transicionar(questionId, hasta, solicitadoPor, motivo, null);
    }

    private QuestionState transicionar(Long questionId, EstadoPregunta hasta, String solicitadoPor, String motivo, String operationId) {
        QuestionState actual = estados.findById(questionId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "La pregunta " + questionId + " no tiene estado"));
        EstadoPregunta desde = actual.getEstado();
        if (desde == hasta) {
            throw new ApiException(HttpStatus.CONFLICT, "La pregunta " + questionId + " ya está en " + desde.etiqueta());
        }
        if (!desde.puedePasarA(hasta)) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "Transición no permitida: " + desde.etiqueta() + " → " + hasta.etiqueta());
        }
        if (hasta == EstadoPregunta.RECHAZADA && (motivo == null || motivo.isBlank())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "El rechazo exige un motivo");
        }
        actual.cambiarA(hasta, solicitadoPor);
        estados.save(actual);
        historial.save(new StateTransition(questionId, desde, hasta, solicitadoPor, motivo));
        eventos.estadoCambiado(questionId, desde, hasta, solicitadoPor, motivo);
        if (hasta == EstadoPregunta.PENDIENTE_REVISION) {
            eventos.preguntaEnviada(actual, operationId);
        } else if (hasta == EstadoPregunta.PUBLICADA) {
            eventos.preguntaPublicada(actual, solicitadoPor);
        }
        return actual;
    }

    public QuestionState obtener(Long questionId) {
        return estados.findById(questionId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "La pregunta " + questionId + " no tiene estado"));
    }

    public List<StateTransition> historialDe(Long questionId) {
        return historial.findByQuestionIdOrderByIdAsc(questionId);
    }

    public OperationResult operacion(String operationId) {
        return resultados.findById(operationId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "No existe la operación " + operationId));
    }

    /**
     * Procesa un comando de RabbitMQ: intenta la transición, registra el resultado (COMPLETED o FAILED con el motivo) y lo
     * publica. Un comando repetido (misma operación) se ignora.
     */
    public void procesarComando(EventEnvelope comando) {
        String operationId = comando.stringOf("operationId");
        Long questionId = comando.longOf("questionId");
        if (operationId == null || resultados.existsById(operationId)) {
            return;
        }
        EstadoPregunta destino = DESTINO_DEL_COMANDO.get(comando.type());
        OperationStatus status;
        String mensaje;
        try {
            if (destino == null || questionId == null) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Comando desconocido o incompleto: " + comando.type());
            }
            QuestionState estado = transicionar(questionId, destino, comando.stringOf("requestedBy"), comando.stringOf("reason"), operationId);
            status = OperationStatus.COMPLETED;
            mensaje = "La pregunta pasó a " + estado.getEstado().etiqueta();
        } catch (ApiException e) {
            status = OperationStatus.FAILED;
            mensaje = e.getMessage();
        } catch (RuntimeException e) {
            status = OperationStatus.FAILED;
            mensaje = "Error al procesar el comando: " + e.getMessage();
        }
        resultados.save(new OperationResult(operationId, questionId, comando.type(), status, mensaje));
        eventos.resultadoDeOperacion(operationId, questionId, comando.type(), status.name(), mensaje);
    }
}
