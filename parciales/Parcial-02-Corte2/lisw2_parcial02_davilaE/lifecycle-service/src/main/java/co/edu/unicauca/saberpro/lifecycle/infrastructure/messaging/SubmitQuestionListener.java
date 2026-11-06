package co.edu.unicauca.saberpro.lifecycle.infrastructure.messaging;

import co.edu.unicauca.saberpro.domain.exception.InvalidTransitionException;
import co.edu.unicauca.saberpro.lifecycle.application.LifecycleService;
import co.edu.unicauca.saberpro.lifecycle.application.SubmitQuestionForReview;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class SubmitQuestionListener {

    private final LifecycleService service;

    public SubmitQuestionListener(LifecycleService service) {
        this.service = service;
    }

    @RabbitListener(queues = RabbitConfig.QUEUE_SUBMIT)
    public void onSubmit(SubmitQuestionForReview command) {
        try {
            service.submit(command);
        } catch (InvalidTransitionException | IllegalArgumentException e) {
            // un mensaje que nunca va a poder aplicarse no se reintenta
            throw new AmqpRejectAndDontRequeueException(e.getMessage(), e);
        }
    }
}
