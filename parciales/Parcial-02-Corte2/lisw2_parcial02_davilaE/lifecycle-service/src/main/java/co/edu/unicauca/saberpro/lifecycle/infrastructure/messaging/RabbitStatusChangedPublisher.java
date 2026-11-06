package co.edu.unicauca.saberpro.lifecycle.infrastructure.messaging;

import co.edu.unicauca.saberpro.lifecycle.application.IStatusChangedPublisher;
import co.edu.unicauca.saberpro.lifecycle.application.QuestionStatusChanged;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class RabbitStatusChangedPublisher implements IStatusChangedPublisher {

    private final RabbitTemplate rabbitTemplate;

    public RabbitStatusChangedPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publish(QuestionStatusChanged event) {
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, RabbitConfig.KEY_STATUS_CHANGED, event);
    }
}
