package co.edu.unicauca.saberpro.lifecycle.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class LifecycleService {

    private static final Logger log = LoggerFactory.getLogger(LifecycleService.class);

    private final EstadoCambiadoPublisher publisher;

    public LifecycleService(EstadoCambiadoPublisher publisher) {
        this.publisher = publisher;
    }

    public EstadoCambiado enviarARevision(SolicitudRevision solicitud) {
        EstadoCiclo actual = EstadoCiclo.desde(solicitud.estadoActual());
        EstadoCiclo nuevo = actual.alEnviarARevision();
        EstadoCambiado evento = new EstadoCambiado(solicitud.operationId(), solicitud.preguntaId(), actual.name(), nuevo.name());
        publisher.publicar(evento);
        log.info("Pregunta {} pasó de {} a {} (operación {})", evento.preguntaId(), actual, nuevo, evento.operationId());
        return evento;
    }
}
