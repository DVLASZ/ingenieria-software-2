package co.edu.unicauca.saberpro.domain.model;

import co.edu.unicauca.saberpro.domain.exception.ReglaNegocioException;

/** Objeto de valor: por qué la clave es la respuesta correcta. Mínimo 15 caracteres y sin expresiones prohibidas. */
public record Justificacion(String texto) {

    public static final int MINIMO_CARACTERES = 15;

    public Justificacion {
        if (texto == null || texto.trim().length() < MINIMO_CARACTERES) {
            throw new ReglaNegocioException(
                    "La justificación debe contener al menos " + MINIMO_CARACTERES + " caracteres.");
        }
        if (FiltroExpresion.contieneExpresionProhibida(texto)) {
            throw new ReglaNegocioException("La justificación usa una expresión prohibida.");
        }
        texto = texto.trim();
    }
}
