package co.edu.unicauca.saberpro.lifecycle.messaging;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import co.edu.unicauca.saberpro.lifecycle.application.LifecycleService;
import co.edu.unicauca.saberpro.lifecycle.application.SolicitudRevision;
import co.edu.unicauca.saberpro.lifecycle.application.TransicionInvalidaException;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;

class QuestionReviewListenerTest {

    final LifecycleService service = mock(LifecycleService.class);
    final QuestionReviewListener listener = new QuestionReviewListener(service);
    final SolicitudRevision solicitud = new SolicitudRevision("op-1", "p-1", "BORRADOR");

    @Test
    void delegaEnElServicio() {
        assertDoesNotThrow(() -> listener.alRecibirSolicitud(solicitud));
        verify(service).enviarARevision(solicitud);
    }

    @Test
    void unaTransicionInvalidaSeRechazaSinVolverALaCola() {
        when(service.enviarARevision(any())).thenThrow(new TransicionInvalidaException("no"));
        assertThrows(AmqpRejectAndDontRequeueException.class, () -> listener.alRecibirSolicitud(solicitud));
    }

    @Test
    void unEstadoDesconocidoSeRechazaSinVolverALaCola() {
        when(service.enviarARevision(any())).thenThrow(new IllegalArgumentException("no"));
        assertThrows(AmqpRejectAndDontRequeueException.class, () -> listener.alRecibirSolicitud(solicitud));
    }
}
