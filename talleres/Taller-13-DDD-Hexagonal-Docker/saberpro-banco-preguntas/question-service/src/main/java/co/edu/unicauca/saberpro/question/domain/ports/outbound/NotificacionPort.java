package co.edu.unicauca.saberpro.question.domain.ports.outbound;

import co.edu.unicauca.saberpro.question.domain.model.Pregunta;

/** Puerto de salida: avisar a otros servicios que una pregunta pide revisión. */
public interface NotificacionPort {

    void solicitarRevision(Pregunta pregunta, String operationId);
}
