package co.edu.unicauca.saberpro.infrastructure.adapter.in.web.dto;

import java.util.List;

public record CrearPreguntaRequest(String enunciado, String justificacion, List<OpcionRequest> opciones) {

    public record OpcionRequest(String texto, boolean correcta) {
    }
}
