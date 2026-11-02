package co.edu.unicauca.saberpro.domain.model;

import co.edu.unicauca.saberpro.domain.exception.ReglaNegocioException;

/** Objeto de valor: lo que un revisor decidió y por qué. Rechazar exige una observación. */
public record Evaluacion(String revisorId, Dictamen dictamen, String observacion) {

    public Evaluacion {
        if (revisorId == null || revisorId.isBlank()) {
            throw new ReglaNegocioException("La evaluación necesita al revisor que la hizo.");
        }
        if (dictamen == null) {
            throw new ReglaNegocioException("La evaluación necesita un dictamen.");
        }
        observacion = observacion == null ? "" : observacion.trim();
        if (dictamen == Dictamen.RECHAZADO && observacion.isEmpty()) {
            throw new ReglaNegocioException("Rechazar una pregunta exige escribir una observación.");
        }
        revisorId = revisorId.trim();
    }
}
