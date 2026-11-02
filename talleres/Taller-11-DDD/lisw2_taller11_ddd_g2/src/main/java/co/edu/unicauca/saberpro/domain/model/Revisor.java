package co.edu.unicauca.saberpro.domain.model;

import co.edu.unicauca.saberpro.domain.exception.ReglaNegocioException;

/** Entidad: un docente que evalúa preguntas. Se identifica por su identificador de docente. */
public class Revisor {

    private final String id;
    private final String nombre;

    public Revisor(String id, String nombre) {
        if (id == null || id.isBlank()) {
            throw new ReglaNegocioException("El revisor necesita el identificador del docente.");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new ReglaNegocioException("El revisor necesita un nombre.");
        }
        this.id = id.trim();
        this.nombre = nombre.trim();
    }

    public String id() {
        return id;
    }

    public String nombre() {
        return nombre;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Revisor otro && id.equals(otro.id));
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
