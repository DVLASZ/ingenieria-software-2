package co.edu.unicauca.simulation.messaging;

import co.edu.unicauca.simulation.config.RabbitMQConfig;
import co.edu.unicauca.simulation.dto.EventEnvelope;
import co.edu.unicauca.simulation.service.SimulationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/** Consume con `@RabbitListener` las preguntas publicadas para agregarlas al catálogo de simulacros. */
@Component
public class PublishedQuestionListener {

    private static final Logger log = LoggerFactory.getLogger(PublishedQuestionListener.class);

    private final SimulationService servicio;

    public PublishedQuestionListener(SimulationService servicio) {
        this.servicio = servicio;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_SIMULATION_PUBLISHED_QUESTIONS)
    public void alPublicarseUnaPregunta(EventEnvelope evento) {
        log.info("[simulation] pregunta {} publicada", evento.longOf("questionId"));
        servicio.agregarAlCatalogo(evento.longOf("questionId"));
    }
}
