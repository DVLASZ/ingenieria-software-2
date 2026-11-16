package co.edu.unicauca.saberpro.question.infrastructure;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import co.edu.unicauca.saberpro.question.domain.exception.PreguntaNoEncontradaException;
import co.edu.unicauca.saberpro.question.domain.model.EstadoPregunta;
import co.edu.unicauca.saberpro.question.domain.ports.inbound.EvaluarPreguntaUseCase;
import co.edu.unicauca.saberpro.question.infrastructure.messaging.QuestionStatusListener;
import co.edu.unicauca.saberpro.question.infrastructure.messaging.QuestionStatusListener.EstadoCambiado;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;

class QuestionStatusListenerTest {

    final EvaluarPreguntaUseCase evaluar = mock(EvaluarPreguntaUseCase.class);
    final QuestionStatusListener listener = new QuestionStatusListener(evaluar);

    @Test
    void confirmaElEstadoRecibido() {
        assertDoesNotThrow(() -> listener.alCambiarElEstado(
                new EstadoCambiado("op-1", "p-1", "BORRADOR", "PENDIENTE_REVISION")));
        verify(evaluar).confirmarEstado("p-1", EstadoPregunta.PENDIENTE_REVISION);
    }

    @Test
    void unEstadoDesconocidoSeRechazaSinVolverALaCola() {
        assertThrows(AmqpRejectAndDontRequeueException.class, () -> listener.alCambiarElEstado(
                new EstadoCambiado("op-1", "p-1", "BORRADOR", "PUBLICADA")));
    }

    @Test
    void unaPreguntaQueNoExisteSeRechazaSinVolverALaCola() {
        doThrow(new PreguntaNoEncontradaException("p-9")).when(evaluar).confirmarEstado(any(), any());
        assertThrows(AmqpRejectAndDontRequeueException.class, () -> listener.alCambiarElEstado(
                new EstadoCambiado("op-1", "p-9", "BORRADOR", "PENDIENTE_REVISION")));
    }
}
