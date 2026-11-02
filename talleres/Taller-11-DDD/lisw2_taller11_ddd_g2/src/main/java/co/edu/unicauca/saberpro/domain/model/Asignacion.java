package co.edu.unicauca.saberpro.domain.model;

import co.edu.unicauca.saberpro.domain.exception.ReglaNegocioException;

/**
 * Objeto de valor: una pregunta le fue asignada a un revisor y todavía no la evalúa, o ya lo hizo. Como es inmutable,
 * evaluar devuelve otra asignación.
 */
public record Asignacion(Revisor revisor, Evaluacion evaluacion) {

    public Asignacion {
        if (revisor == null) {
            throw new ReglaNegocioException("La asignación necesita un revisor.");
        }
        if (evaluacion != null && !evaluacion.revisorId().equals(revisor.id())) {
            throw new ReglaNegocioException("La evaluación no es de este revisor.");
        }
    }

    public static Asignacion de(Revisor revisor) {
        return new Asignacion(revisor, null);
    }

    public boolean pendiente() {
        return evaluacion == null;
    }

    public Asignacion evaluar(Evaluacion nueva) {
        if (!pendiente()) {
            throw new ReglaNegocioException("El revisor " + revisor.nombre() + " ya evaluó esta pregunta.");
        }
        return new Asignacion(revisor, nueva);
    }
}
