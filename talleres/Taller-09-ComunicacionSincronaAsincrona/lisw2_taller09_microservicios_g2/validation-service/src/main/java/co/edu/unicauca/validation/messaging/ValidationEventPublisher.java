package co.edu.unicauca.validation.messaging;

import co.edu.unicauca.validation.dto.EventEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Publica `ValidationFailed` con {@link RabbitTemplate} para que se avise al autor sin bloquear la respuesta. */
@Component
public class ValidationEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(ValidationEventPublisher.class);
    public static final String ROUTING_KEY = "validation.failed";

    private final RabbitTemplate rabbitTemplate;
    private final String topicExchange;

    public ValidationEventPublisher(RabbitTemplate rabbitTemplate, @Value("${app.messaging.topic-exchange}") String topicExchange) {
        this.rabbitTemplate = rabbitTemplate;
        this.topicExchange = topicExchange;
    }

    public void validacionFallida(Long autorId, String titulo, List<String> errores) {
        Map<String, Object> data = new HashMap<>();
        data.put("authorId", autorId);
        data.put("title", titulo);
        data.put("errors", errores);
        try {
            rabbitTemplate.convertAndSend(topicExchange, ROUTING_KEY, EventEnvelope.of("ValidationFailed", data));
        } catch (AmqpException e) {
            log.warn("No se pudo publicar ValidationFailed: {}", e.getMessage());
        }
    }
}
