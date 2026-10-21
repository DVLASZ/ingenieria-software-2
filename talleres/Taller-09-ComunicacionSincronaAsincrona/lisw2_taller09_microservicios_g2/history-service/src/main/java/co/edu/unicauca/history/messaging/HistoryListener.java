package co.edu.unicauca.history.messaging;

import co.edu.unicauca.history.config.RabbitMQConfig;
import co.edu.unicauca.history.dto.EventEnvelope;
import co.edu.unicauca.history.service.HistoryService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/** Consume con `@RabbitListener` todos los eventos del exchange de eventos (binding `#`). */
@Component
public class HistoryListener {

    private final HistoryService servicio;

    public HistoryListener(HistoryService servicio) {
        this.servicio = servicio;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_HISTORY_EVENTS)
    public void alRecibirUnEvento(EventEnvelope evento) {
        servicio.registrar(evento);
    }
}
