package co.edu.unicauca.saberpro.domain.model;

import co.edu.unicauca.saberpro.domain.exception.ReglaNegocioException;

/** Objeto de valor: una opción de respuesta, con su texto y si es la clave. */
public record OpcionRespuesta(String texto, boolean esCorrecta) {

    public OpcionRespuesta {
        if (texto == null || texto.isBlank()) {
            throw new ReglaNegocioException("El texto de una opción de respuesta no puede estar vacío.");
        }
        if (FiltroExpresion.contieneExpresionProhibida(texto)) {
            throw new ReglaNegocioException(
                    "Una opción no puede usar expresiones como \"todas las anteriores\" o \"ninguna de las anteriores\".");
        }
        texto = texto.trim();
    }

    /** Dos opciones repiten el texto si solo se diferencian en mayúsculas, tildes o espacios. */
    public boolean mismoTextoQue(OpcionRespuesta otra) {
        return FiltroExpresion.normalizar(texto).equals(FiltroExpresion.normalizar(otra.texto));
    }
}
