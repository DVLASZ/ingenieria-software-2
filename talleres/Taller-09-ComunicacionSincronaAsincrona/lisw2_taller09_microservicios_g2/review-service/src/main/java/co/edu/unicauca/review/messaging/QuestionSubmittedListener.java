package co.edu.unicauca.review.messaging;

import co.edu.unicauca.review.config.RabbitMQConfig;
import co.edu.unicauca.review.dto.EventEnvelope;
import co.edu.unicauca.review.service.ReviewService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/** Consume con `@RabbitListener` las preguntas enviadas a revisión para abrir su caso (ronda). */
@Component
public class QuestionSubmittedListener {

    private static final Logger log = LoggerFactory.getLogger(QuestionSubmittedListener.class);

    private final ReviewService servicio;

    public QuestionSubmittedListener(ReviewService servicio) {
        this.servicio = servicio;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_REVIEW_SUBMITTED_QUESTIONS)
    public void alEnviarseUnaPregunta(EventEnvelope mensaje) {
        log.info("[review] pregunta {} enviada a revisión", mensaje.longOf("questionId"));
        servicio.abrirCaso(mensaje.eventId(), mensaje.longOf("questionId"), mensaje.longOf("authorId"), mensaje.stringOf("title"));
    }
}
