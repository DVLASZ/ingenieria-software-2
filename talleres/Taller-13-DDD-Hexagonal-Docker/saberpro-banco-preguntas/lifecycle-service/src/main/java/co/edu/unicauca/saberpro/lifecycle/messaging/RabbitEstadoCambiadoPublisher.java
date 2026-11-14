package co.edu.unicauca.saberpro.lifecycle.messaging;

import co.edu.unicauca.saberpro.lifecycle.application.EstadoCambiado;
import co.edu.unicauca.saberpro.lifecycle.application.EstadoCambiadoPublisher;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class RabbitEstadoCambiadoPublisher implements EstadoCambiadoPublisher {

    private final RabbitTemplate rabbitTemplate;

    public RabbitEstadoCambiadoPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publicar(EstadoCambiado evento) {
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, RabbitConfig.KEY_ESTADO, evento);
    }
}
