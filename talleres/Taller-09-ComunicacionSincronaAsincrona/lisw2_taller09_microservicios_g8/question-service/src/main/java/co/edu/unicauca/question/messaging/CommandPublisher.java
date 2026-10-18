package co.edu.unicauca.question.messaging;

import co.edu.unicauca.question.dto.EventEnvelope;
import co.edu.unicauca.question.model.Comando;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/** Publica con {@link RabbitTemplate} los comandos dirigidos a lifecycle-service (exchange direct). */
@Component
public class CommandPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final String directExchange;

    public CommandPublisher(RabbitTemplate rabbitTemplate, @Value("${app.messaging.direct-exchange}") String directExchange) {
        this.rabbitTemplate = rabbitTemplate;
        this.directExchange = directExchange;
    }

    public void enviar(Comando comando, Long questionId, String operationId, String requestedBy, String motivo) {
        Map<String, Object> data = new HashMap<>();
        data.put("operationId", operationId);
        data.put("questionId", questionId);
        data.put("requestedBy", requestedBy);
        data.put("requestedAt", java.time.Instant.now().toString());
        data.put("reason", motivo);
        rabbitTemplate.convertAndSend(directExchange, comando.routingKey(), EventEnvelope.of(comando.tipo(), data));
    }
}
