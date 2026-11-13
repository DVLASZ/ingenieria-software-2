package co.edu.unicauca.saberpro.infrastructure.adapter.out.notification;

import co.edu.unicauca.saberpro.domain.model.Pregunta;
import co.edu.unicauca.saberpro.domain.ports.outbound.NotificadorPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsolaNotificadorAdapter implements NotificadorPort {

    private static final Logger log = LoggerFactory.getLogger(ConsolaNotificadorAdapter.class);

    @Override
    public void preguntaEnviadaARevision(Pregunta pregunta) {
        log.info("Pregunta {} lista para revisión", pregunta.id());
    }
}
