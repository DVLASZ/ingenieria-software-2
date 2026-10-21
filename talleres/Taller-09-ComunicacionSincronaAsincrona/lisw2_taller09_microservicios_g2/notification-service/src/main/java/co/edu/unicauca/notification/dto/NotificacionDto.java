package co.edu.unicauca.notification.dto;

import co.edu.unicauca.notification.model.Notificacion;
import co.edu.unicauca.notification.model.NotificationType;

import java.time.Instant;

public record NotificacionDto(Long id, Long destinatarioId, NotificationType tipo, Long questionId, String asunto,
                              String mensaje, String estado, Instant creadaEn) {

    public static NotificacionDto de(Notificacion n) {
        return new NotificacionDto(n.getId(), n.getDestinatarioId(), n.getTipo(), n.getQuestionId(), n.getAsunto(),
                n.getMensaje(), n.getEstado(), n.getCreadaEn());
    }
}
