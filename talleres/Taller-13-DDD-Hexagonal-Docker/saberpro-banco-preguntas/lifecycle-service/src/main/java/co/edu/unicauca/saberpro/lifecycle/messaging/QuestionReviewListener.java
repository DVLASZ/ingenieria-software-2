package co.edu.unicauca.saberpro.lifecycle.messaging;

import co.edu.unicauca.saberpro.lifecycle.application.LifecycleService;
import co.edu.unicauca.saberpro.lifecycle.application.SolicitudRevision;
import co.edu.unicauca.saberpro.lifecycle.application.TransicionInvalidaException;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class QuestionReviewListener {

    private final LifecycleService service;

    public QuestionReviewListener(LifecycleService service) {
        this.service = service;
    }

    @RabbitListener(queues = RabbitConfig.QUEUE_REVISAR)
    public void alRecibirSolicitud(SolicitudRevision solicitud) {
        try {
            service.enviarARevision(solicitud);
        } catch (TransicionInvalidaException | IllegalArgumentException e) {
            // un mensaje que nunca se va a poder aplicar no vuelve a la cola
            throw new AmqpRejectAndDontRequeueException(e.getMessage(), e);
        }
    }
}
