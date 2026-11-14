package co.edu.unicauca.saberpro.lifecycle.application;

/** Evento con el que este servicio avisa que la pregunta cambió de estado. */
public record EstadoCambiado(String operationId, String preguntaId, String estadoAnterior, String estadoNuevo) {
}
