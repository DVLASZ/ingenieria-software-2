package co.edu.unicauca.notification.service;

import co.edu.unicauca.notification.dto.EventEnvelope;
import co.edu.unicauca.notification.model.Notificacion;
import co.edu.unicauca.notification.model.NotificationType;
import co.edu.unicauca.notification.repository.NotificacionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/** Decide a quién se avisa y qué se dice según el evento; el envío es simulado. */
@Service
public class NotificationService {

    private final NotificacionRepository notificaciones;
    private final EmailSimulator correo;

    public NotificationService(NotificacionRepository notificaciones, EmailSimulator correo) {
        this.notificaciones = notificaciones;
        this.correo = correo;
    }

    public void procesar(EventEnvelope e) {
        Long questionId = e.longOf("questionId");
        String titulo = String.valueOf(e.data().getOrDefault("title", "sin título"));
        switch (e.type()) {
            case "ReviewerAssigned" -> avisar(e, e.longOf("reviewerId"), NotificationType.REVIEWER_ASSIGNED, questionId,
                    "Pregunta asignada para revisión",
                    "Se te asignó la pregunta «" + titulo + "» (ronda " + e.data().get("round") + ") para revisarla");
            case "EvaluationSubmitted" -> {
                String texto = "El revisor " + e.longOf("reviewerId") + " evaluó la pregunta «" + titulo + "»: " + e.stringOf("decision");
                avisar(e, e.longOf("authorId"), NotificationType.EVALUATION_SUBMITTED, questionId, "Nueva evaluación recibida", texto);
                avisar(e, e.longOf("adminId"), NotificationType.EVALUATION_SUBMITTED, questionId, "Nueva evaluación recibida", texto);
            }
            case "ReviewCompleted" -> {
                boolean aprobada = "APPROVED".equals(e.stringOf("result"));
                NotificationType tipo = aprobada ? NotificationType.QUESTION_APPROVED : NotificationType.QUESTION_REJECTED;
                String texto = "La revisión de la pregunta «" + titulo + "» terminó: quedó " + (aprobada ? "aprobada" : "rechazada");
                avisar(e, e.longOf("authorId"), tipo, questionId, "Resultado de la revisión", texto);
                avisar(e, e.longOf("adminId"), tipo, questionId, "Resultado de la revisión", texto);
            }
            case "ValidationFailed" -> avisar(e, e.longOf("authorId"), NotificationType.VALIDATION_FAILED, questionId,
                    "Tu pregunta no pasó la validación", "La pregunta «" + titulo + "» tiene errores: " + e.data().get("errors"));
            case "question.published" -> avisar(e, e.longOf("authorId"), NotificationType.QUESTION_PUBLISHED, questionId,
                    "Tu pregunta fue publicada", "La pregunta «" + titulo + "» ya está publicada en el banco");
            default -> {
                // otros eventos no generan notificaciones
            }
        }
    }

    public List<Notificacion> buscar(Long destinatario, NotificationType tipo) {
        return notificaciones.buscar(destinatario, tipo);
    }

    private void avisar(EventEnvelope e, Long destinatario, NotificationType tipo, Long questionId, String asunto, String mensaje) {
        if (destinatario == null || notificaciones.existsByEventIdAndDestinatarioId(e.eventId(), destinatario)) {
            return;
        }
        correo.enviar(destinatario, asunto, mensaje);
        notificaciones.save(new Notificacion(e.eventId(), destinatario, tipo, questionId, asunto, mensaje));
    }

}
