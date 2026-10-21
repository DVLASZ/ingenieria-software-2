package co.edu.unicauca.simulation.messaging;

import co.edu.unicauca.simulation.dto.EventEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/** Publica con {@link RabbitTemplate} el evento `SimulationCompleted` cuando alguien resuelve un simulacro. */
@Component
public class SimulationEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(SimulationEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;
    private final String topicExchange;

    public SimulationEventPublisher(RabbitTemplate rabbitTemplate, @Value("${app.messaging.topic-exchange}") String topicExchange) {
        this.rabbitTemplate = rabbitTemplate;
        this.topicExchange = topicExchange;
    }

    public void simulacroResuelto(Long simulationId, Long userId, int correctas, int total, double puntaje, Map<String, Map<String, Integer>> porCompetencia) {
        Map<String, Object> data = new HashMap<>();
        data.put("simulationId", simulationId);
        data.put("userId", userId);
        data.put("correct", correctas);
        data.put("total", total);
        data.put("score", puntaje);
        data.put("byCompetency", porCompetencia);
        try {
            rabbitTemplate.convertAndSend(topicExchange, "simulation.completed", EventEnvelope.of("SimulationCompleted", data));
        } catch (AmqpException e) {
            log.warn("No se pudo publicar SimulationCompleted: {}", e.getMessage());
        }
    }
}
