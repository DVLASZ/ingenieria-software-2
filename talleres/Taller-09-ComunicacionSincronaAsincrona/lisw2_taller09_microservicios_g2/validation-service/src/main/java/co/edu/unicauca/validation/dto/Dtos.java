package co.edu.unicauca.validation.dto;

import java.util.List;

/** Contratos REST del servicio de validación. */
public final class Dtos {

    private Dtos() {
    }

    /** Pregunta que se quiere validar; `autorId` y `titulo` solo sirven para avisar si falla. */
    public record ValidationRequest(Long autorId, String titulo, String contexto, String pregunta,
                                    List<String> opciones, String respuestaCorrecta) {
    }

    public record ValidationResult(boolean valid, List<String> errors) {
    }
}
