package co.edu.unicauca.lifecycle.messaging;

import co.edu.unicauca.lifecycle.dto.EventEnvelope;
import co.edu.unicauca.lifecycle.model.EstadoPregunta;
import co.edu.unicauca.lifecycle.model.QuestionState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/** Publica con {@link RabbitTemplate} el resultado de las operaciones (exchange direct) y los eventos (exchange topic). */
@Component
public class LifecycleEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(LifecycleEventPublisher.class);

    public static final String KEY_OPERATION_RESULT = "question.operation-result";

    private final RabbitTemplate rabbitTemplate;
    private final String directExchange;
    private final String topicExchange;

    public LifecycleEventPublisher(RabbitTemplate rabbitTemplate,
                                   @Value("${app.messaging.direct-exchange}") String directExchange,
                                   @Value("${app.messaging.topic-exchange}") String topicExchange) {
        this.rabbitTemplate = rabbitTemplate;
        this.directExchange = directExchange;
        this.topicExchange = topicExchange;
    }

    public void resultadoDeOperacion(String operationId, Long questionId, String tipo, String status, String mensaje) {
        Map<String, Object> data = new HashMap<>();
        data.put("operationId", operationId);
        data.put("questionId", questionId);
        data.put("type", tipo);
        data.put("status", status);
        data.put("message", mensaje);
        enviar(directExchange, KEY_OPERATION_RESULT, "OperationResult", data);
    }

    public void estadoCambiado(Long questionId, EstadoPregunta desde, EstadoPregunta hasta, String por, String motivo) {
        Map<String, Object> data = new HashMap<>();
        data.put("questionId", questionId);
        data.put("from", desde == null ? null : desde.name());
        data.put("to", hasta.name());
        data.put("changedBy", por);
        data.put("reason", motivo);
        enviar(topicExchange, "question.state-changed", "question.state-changed", data);
    }

    /** `QuestionSubmittedForReview`: la pregunta pasó a PENDIENTE_REVISION (con el identificador de la operación si vino de un comando). */
    public void preguntaEnviada(QuestionState s, String operationId) {
        Map<String, Object> data = datosDePregunta(s);
        data.put("operationId", operationId);
        data.put("newState", EstadoPregunta.PENDIENTE_REVISION.name());
        enviar(topicExchange, "question.submitted", "QuestionSubmittedForReview", data);
    }

    public void preguntaPublicada(QuestionState s, String por) {
        Map<String, Object> data = datosDePregunta(s);
        data.put("publishedBy", por);
        enviar(topicExchange, "question.published", "question.published", data);
    }

    private Map<String, Object> datosDePregunta(QuestionState s) {
        Map<String, Object> data = new HashMap<>();
        data.put("questionId", s.getQuestionId());
        data.put("authorId", s.getAutorId());
        data.put("title", s.getTitulo());
        data.put("competency", s.getCompetencia());
        return data;
    }

    private void enviar(String exchange, String routingKey, String tipo, Map<String, Object> data) {
        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, EventEnvelope.of(tipo, data));
        } catch (AmqpException e) {
            log.warn("No se pudo publicar {}: {}", routingKey, e.getMessage());
        }
    }
}
