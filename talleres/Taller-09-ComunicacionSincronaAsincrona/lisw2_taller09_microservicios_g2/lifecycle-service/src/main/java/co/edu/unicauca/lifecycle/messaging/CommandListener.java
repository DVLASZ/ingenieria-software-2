package co.edu.unicauca.lifecycle.messaging;

import co.edu.unicauca.lifecycle.config.RabbitMQConfig;
import co.edu.unicauca.lifecycle.dto.EventEnvelope;
import co.edu.unicauca.lifecycle.service.LifecycleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/** Consume con `@RabbitListener` los comandos que piden un cambio de estado. */
@Component
public class CommandListener {

    private static final Logger log = LoggerFactory.getLogger(CommandListener.class);

    private final LifecycleService servicio;

    public CommandListener(LifecycleService servicio) {
        this.servicio = servicio;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_LIFECYCLE_COMMANDS)
    public void alRecibirUnComando(EventEnvelope comando) {
        log.info("[lifecycle] comando {} para la pregunta {} (operación {})", comando.type(),
                comando.longOf("questionId"), comando.stringOf("operationId"));
        servicio.procesarComando(comando);
    }
}
