package co.edu.unicauca.saberpro.domain.model;

import co.edu.unicauca.saberpro.domain.exception.ReglaNegocioException;

/** Objeto de valor: el texto de una opción y si es la clave. */
public record OpcionRespuesta(String texto, boolean esCorrecta) {

    public OpcionRespuesta {
        if (texto == null || texto.isBlank()) {
            throw new ReglaNegocioException("El texto de una opción no puede estar vacío.");
        }
        texto = texto.trim();
    }
}
