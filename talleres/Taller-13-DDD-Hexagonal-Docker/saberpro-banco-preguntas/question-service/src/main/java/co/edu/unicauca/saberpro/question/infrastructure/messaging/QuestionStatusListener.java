package co.edu.unicauca.saberpro.question.infrastructure.messaging;

import co.edu.unicauca.saberpro.question.domain.exception.DomainException;
import co.edu.unicauca.saberpro.question.domain.model.EstadoPregunta;
import co.edu.unicauca.saberpro.question.domain.ports.inbound.EvaluarPreguntaUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/** Adaptador de entrada: recibe el aviso de lifecycle-service y se lo pasa al puerto. */
@Component
public class QuestionStatusListener {

    private static final Logger log = LoggerFactory.getLogger(QuestionStatusListener.class);

    private final EvaluarPreguntaUseCase evaluar;

    public QuestionStatusListener(EvaluarPreguntaUseCase evaluar) {
        this.evaluar = evaluar;
    }

    @RabbitListener(queues = RabbitConfig.QUEUE_ESTADO)
    public void alCambiarElEstado(EstadoCambiado evento) {
        try {
            evaluar.confirmarEstado(evento.preguntaId(), EstadoPregunta.valueOf(evento.estadoNuevo()));
            log.info("Pregunta {} confirmada en {} (operación {})", evento.preguntaId(), evento.estadoNuevo(), evento.operationId());
        } catch (DomainException | IllegalArgumentException e) {
            // un aviso que nunca se va a poder aplicar no vuelve a la cola
            throw new AmqpRejectAndDontRequeueException(e.getMessage(), e);
        }
    }

    public record EstadoCambiado(String operationId, String preguntaId, String estadoAnterior, String estadoNuevo) {
    }
}
