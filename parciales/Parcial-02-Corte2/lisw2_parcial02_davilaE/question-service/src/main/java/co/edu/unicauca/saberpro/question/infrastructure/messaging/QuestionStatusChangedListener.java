package co.edu.unicauca.saberpro.question.infrastructure.messaging;

import co.edu.unicauca.saberpro.domain.model.QuestionStatus;
import co.edu.unicauca.saberpro.question.application.QuestionService;
import co.edu.unicauca.saberpro.question.application.event.QuestionStatusChanged;
import co.edu.unicauca.saberpro.question.application.exception.QuestionNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/** cuando lifecycle-service cambia el estado, aquí se refleja en la base de datos */
@Component
public class QuestionStatusChangedListener {

    private static final Logger log = LoggerFactory.getLogger(QuestionStatusChangedListener.class);

    private final QuestionService service;

    public QuestionStatusChangedListener(QuestionService service) {
        this.service = service;
    }

    @RabbitListener(queues = RabbitConfig.QUEUE_STATUS_CHANGED)
    public void onStatusChanged(QuestionStatusChanged event) {
        try {
            service.applyStatus(event.questionId(), QuestionStatus.valueOf(event.status()));
            log.info("Pregunta {} ahora está {} (operationId={})", event.questionId(), event.status(), event.operationId());
        } catch (QuestionNotFoundException | IllegalArgumentException e) {
            // no tiene sentido reintentar un mensaje que no se puede aplicar
            throw new AmqpRejectAndDontRequeueException(e.getMessage(), e);
        }
    }
}
