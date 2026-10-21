package co.edu.unicauca.history.dto;

import co.edu.unicauca.history.model.Entrada;

import java.time.Instant;

public record EntradaDto(Long id, String eventId, String tipo, Long questionId, String actor, String resumen,
                         String detalles, Instant ocurridaEn, Instant registradaEn) {

    public static EntradaDto de(Entrada e) {
        return new EntradaDto(e.getId(), e.getEventId(), e.getTipo(), e.getQuestionId(), e.getActor(), e.getResumen(),
                e.getDetalles(), e.getOcurridaEn(), e.getRegistradaEn());
    }
}
