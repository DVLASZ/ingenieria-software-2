package co.edu.unicauca.saberpro.question.infrastructure.messaging;

import co.edu.unicauca.saberpro.question.domain.exception.ServicioNoDisponibleException;
import co.edu.unicauca.saberpro.question.domain.model.Pregunta;
import co.edu.unicauca.saberpro.question.domain.ports.outbound.NotificacionPort;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/** Adaptador de salida: publica en RabbitMQ el comando que recibe lifecycle-service. */
@Component
public class RabbitMQPublisher implements NotificacionPort {

    private final RabbitTemplate rabbitTemplate;

    public RabbitMQPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void solicitarRevision(Pregunta pregunta, String operationId) {
        SolicitudRevision mensaje = new SolicitudRevision(operationId, pregunta.id(), pregunta.estado().name());
        try {
            rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, RabbitConfig.KEY_REVISAR, mensaje);
        } catch (AmqpException e) {
            throw new ServicioNoDisponibleException("No se pudo publicar en RabbitMQ: " + e.getMessage());
        }
    }

    public record SolicitudRevision(String operationId, String preguntaId, String estadoActual) {
    }
}
