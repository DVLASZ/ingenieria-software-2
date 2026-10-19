package co.edu.unicauca.review.messaging;

import co.edu.unicauca.review.dto.EventEnvelope;
import co.edu.unicauca.review.model.CasoDeRevision;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Publica con {@link RabbitTemplate} los eventos de la revisión. Si el broker falla, solo se registra: la asignación o la
 * evaluación ya quedaron hechas y la notificación no debe bloquearlas.
 */
@Component
public class ReviewEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(ReviewEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;
    private final String topicExchange;

    public ReviewEventPublisher(RabbitTemplate rabbitTemplate, @Value("${app.messaging.topic-exchange}") String topicExchange) {
        this.rabbitTemplate = rabbitTemplate;
        this.topicExchange = topicExchange;
    }

    /** Un `ReviewerAssigned` por cada revisor asignado (routing key `reviewer.assigned`, como en la Figura 2 del enunciado). */
    public void revisoresAsignados(CasoDeRevision caso, List<Long> revisores) {
        for (Long revisor : revisores) {
            Map<String, Object> data = base(caso);
            data.put("reviewerId", revisor);
            data.put("assignedBy", caso.getAsignadoPor());
            enviar("reviewer.assigned", "ReviewerAssigned", data);
        }
    }

    public void evaluacionEnviada(CasoDeRevision caso, Long revisor, String decision, String observacion) {
        Map<String, Object> data = base(caso);
        data.put("reviewerId", revisor);
        data.put("decision", decision);
        data.put("observation", observacion);
        data.put("adminId", caso.getAsignadoPor());
        enviar("review.evaluation-submitted", "EvaluationSubmitted", data);
    }

    public void revisionCompletada(CasoDeRevision caso, String resultado, List<String> observaciones) {
        Map<String, Object> data = base(caso);
        data.put("result", resultado);
        data.put("observations", observaciones);
        data.put("adminId", caso.getAsignadoPor());
        enviar("review.completed", "ReviewCompleted", data);
    }

    private Map<String, Object> base(CasoDeRevision caso) {
        Map<String, Object> data = new HashMap<>();
        data.put("questionId", caso.getQuestionId());
        data.put("authorId", caso.getAutorId());
        data.put("title", caso.getTitulo());
        data.put("round", caso.getRonda());
        return data;
    }

    private void enviar(String routingKey, String tipo, Map<String, Object> data) {
        try {
            rabbitTemplate.convertAndSend(topicExchange, routingKey, EventEnvelope.of(tipo, data));
        } catch (AmqpException e) {
            log.warn("No se pudo publicar {}: {}", routingKey, e.getMessage());
        }
    }
}
