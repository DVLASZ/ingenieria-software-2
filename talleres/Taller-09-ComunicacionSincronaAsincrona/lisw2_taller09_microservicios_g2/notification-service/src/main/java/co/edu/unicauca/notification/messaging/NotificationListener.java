package co.edu.unicauca.notification.messaging;

import co.edu.unicauca.notification.config.RabbitMQConfig;
import co.edu.unicauca.notification.dto.EventEnvelope;
import co.edu.unicauca.notification.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/** Consume con `@RabbitListener` los eventos que generan notificaciones. */
@Component
public class NotificationListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationListener.class);

    private final NotificationService servicio;

    public NotificationListener(NotificationService servicio) {
        this.servicio = servicio;
    }

    /** Cola `notification.reviewer-assigned` de la Figura 2: avisa al revisor asignado. */
    @RabbitListener(queues = RabbitMQConfig.QUEUE_NOTIFICATION_REVIEWER_ASSIGNED)
    public void alAsignarseUnRevisor(EventEnvelope evento) {
        log.info("[notification] revisor asignado a la pregunta {}", evento.longOf("questionId"));
        servicio.procesar(evento);
    }

    /** Resto de los eventos que generan avisos (evaluación, resultado, validación fallida y publicación). */
    @RabbitListener(queues = RabbitMQConfig.QUEUE_NOTIFICATION_EVENTS)
    public void alRecibirUnEvento(EventEnvelope evento) {
        log.info("[notification] evento {}", evento.type());
        servicio.procesar(evento);
    }
}
