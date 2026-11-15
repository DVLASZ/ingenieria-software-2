package co.edu.unicauca.saberpro.question.domain.ports.inbound;

import co.edu.unicauca.saberpro.question.domain.model.EstadoPregunta;

/** Puerto de entrada: el recorrido de la pregunta hacia la revisión. */
public interface EvaluarPreguntaUseCase {

    /** Verifica las invariantes y pide el cambio de estado. No espera la respuesta del ciclo de vida. */
    SolicitudAceptada enviarARevision(String preguntaId);

    /** El ciclo de vida confirmó un cambio de estado. Es idempotente: un aviso repetido no hace nada. */
    void confirmarEstado(String preguntaId, EstadoPregunta nuevoEstado);

    record SolicitudAceptada(String operationId, String preguntaId) {
    }
}
