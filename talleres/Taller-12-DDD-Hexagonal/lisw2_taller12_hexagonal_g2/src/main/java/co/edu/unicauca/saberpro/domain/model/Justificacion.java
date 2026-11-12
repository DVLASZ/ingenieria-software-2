package co.edu.unicauca.saberpro.domain.model;

import co.edu.unicauca.saberpro.domain.exception.ReglaNegocioException;

/** Objeto de valor: la sustentación técnica de la clave. Pide al menos 15 caracteres. */
public record Justificacion(String texto) {

    public static final int MINIMO_CARACTERES = 15;

    public Justificacion {
        if (texto == null || texto.trim().length() < MINIMO_CARACTERES) {
            throw new ReglaNegocioException(
                    "La justificación debe tener al menos " + MINIMO_CARACTERES + " caracteres.");
        }
        texto = texto.trim();
    }
}
