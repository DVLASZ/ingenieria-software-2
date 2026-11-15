package co.edu.unicauca.saberpro.question.domain.model;

import co.edu.unicauca.saberpro.question.domain.exception.ReglaNegocioException;

/** Objeto de valor: el texto de la pregunta. */
public record Enunciado(String texto) {

    public Enunciado {
        if (texto == null || texto.isBlank()) {
            throw new ReglaNegocioException("El enunciado no puede ser nulo ni estar vacío.");
        }
        texto = texto.trim();
    }
}
