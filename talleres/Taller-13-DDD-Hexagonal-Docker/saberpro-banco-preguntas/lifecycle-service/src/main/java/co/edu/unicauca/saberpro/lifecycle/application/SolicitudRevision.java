package co.edu.unicauca.saberpro.lifecycle.application;

/** Comando que manda question-service cuando una pregunta se envía a revisión. */
public record SolicitudRevision(String operationId, String preguntaId, String estadoActual) {
}
