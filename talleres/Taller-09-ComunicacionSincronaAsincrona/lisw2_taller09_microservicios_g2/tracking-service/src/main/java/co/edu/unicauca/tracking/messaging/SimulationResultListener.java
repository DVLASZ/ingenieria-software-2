package co.edu.unicauca.tracking.messaging;

import co.edu.unicauca.tracking.config.RabbitMQConfig;
import co.edu.unicauca.tracking.dto.EventEnvelope;
import co.edu.unicauca.tracking.service.TrackingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/** Consume con `@RabbitListener` los simulacros resueltos para actualizar las estadísticas. */
@Component
public class SimulationResultListener {

    private static final Logger log = LoggerFactory.getLogger(SimulationResultListener.class);

    private final TrackingService servicio;

    public SimulationResultListener(TrackingService servicio) {
        this.servicio = servicio;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_TRACKING_SIMULATION_RESULTS)
    public void alResolverseUnSimulacro(EventEnvelope evento) {
        log.info("[tracking] simulacro {} del usuario {}", evento.longOf("simulationId"), evento.longOf("userId"));
        servicio.registrar(evento);
    }
}
