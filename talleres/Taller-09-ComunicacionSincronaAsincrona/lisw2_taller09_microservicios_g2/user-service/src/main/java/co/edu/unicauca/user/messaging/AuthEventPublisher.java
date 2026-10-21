package co.edu.unicauca.user.messaging;

import co.edu.unicauca.user.dto.EventEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/** Publica en RabbitMQ los eventos de inicio de sesión, con {@link RabbitTemplate}. */
@Component
public class AuthEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(AuthEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;
    private final String topicExchange;

    public AuthEventPublisher(RabbitTemplate rabbitTemplate, @Value("${app.messaging.topic-exchange}") String topicExchange) {
        this.rabbitTemplate = rabbitTemplate;
        this.topicExchange = topicExchange;
    }

    public void loginExitoso(Long userId, String username) {
        publicar("auth.login-succeeded", Map.of("userId", userId, "username", username));
    }

    public void loginFallido(String username) {
        Map<String, Object> data = new HashMap<>();
        data.put("username", username);
        publicar("auth.login-failed", data);
    }

    /** Un fallo del broker no debe impedir responder al login. */
    private void publicar(String routingKey, Map<String, Object> data) {
        try {
            rabbitTemplate.convertAndSend(topicExchange, routingKey, EventEnvelope.of(routingKey, data));
        } catch (AmqpException e) {
            log.warn("No se pudo publicar {}: {}", routingKey, e.getMessage());
        }
    }
}
