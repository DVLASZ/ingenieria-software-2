package co.edu.unicauca.saberpro.question.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import co.edu.unicauca.saberpro.question.domain.exception.ServicioNoDisponibleException;
import co.edu.unicauca.saberpro.question.domain.model.Pregunta;
import co.edu.unicauca.saberpro.question.infrastructure.messaging.RabbitConfig;
import co.edu.unicauca.saberpro.question.infrastructure.messaging.RabbitMQPublisher;
import co.edu.unicauca.saberpro.question.infrastructure.messaging.RabbitMQPublisher.SolicitudRevision;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

class RabbitMQPublisherTest {

    final RabbitTemplate plantilla = mock(RabbitTemplate.class);
    final RabbitMQPublisher publicador = new RabbitMQPublisher(plantilla);

    @Test
    void publicaElComandoEnElExchangeConLaLlaveDeRevision() {
        Pregunta p = PreguntaRepositoryPortContractTest.nueva();
        publicador.solicitarRevision(p, "op-1");
        verify(plantilla).convertAndSend(RabbitConfig.EXCHANGE, RabbitConfig.KEY_REVISAR,
                new SolicitudRevision("op-1", p.id(), "BORRADOR"));
    }

    @Test
    void siRabbitNoResponde503() {
        doThrow(new AmqpConnectException(new RuntimeException("sin conexión")))
                .when(plantilla).convertAndSend(eq(RabbitConfig.EXCHANGE), eq(RabbitConfig.KEY_REVISAR), any(Object.class));
        assertThrows(ServicioNoDisponibleException.class,
                () -> publicador.solicitarRevision(PreguntaRepositoryPortContractTest.nueva(), "op-2"));
    }

    @Test
    void losNombresDeLaTopologiaSonLosDelTaller() {
        assertEquals("pregunta.revisar.queue", RabbitConfig.QUEUE_REVISAR);
        assertEquals("saberpro.direct.exchange", RabbitConfig.EXCHANGE);
    }
}
