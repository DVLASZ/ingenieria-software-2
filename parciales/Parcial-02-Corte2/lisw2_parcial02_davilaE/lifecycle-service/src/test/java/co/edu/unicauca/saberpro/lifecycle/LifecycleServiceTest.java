package co.edu.unicauca.saberpro.lifecycle;

import co.edu.unicauca.saberpro.domain.exception.InvalidTransitionException;
import co.edu.unicauca.saberpro.lifecycle.application.IStatusChangedPublisher;
import co.edu.unicauca.saberpro.lifecycle.application.LifecycleService;
import co.edu.unicauca.saberpro.lifecycle.application.QuestionStatusChanged;
import co.edu.unicauca.saberpro.lifecycle.application.SubmitQuestionForReview;
import co.edu.unicauca.saberpro.lifecycle.infrastructure.messaging.RabbitStatusChangedPublisher;
import co.edu.unicauca.saberpro.lifecycle.infrastructure.messaging.SubmitQuestionListener;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class LifecycleServiceTest {

    IStatusChangedPublisher publisher = mock(IStatusChangedPublisher.class);
    LifecycleService service = new LifecycleService(publisher);

    @Test
    void pasaDeBorradorAPendienteDeRevisionYAvisa() {
        var event = service.submit(new SubmitQuestionForReview("op-1", 7L, "DRAFT"));

        assertThat(event.status()).isEqualTo("PENDING_REVIEW");
        assertThat(event.questionId()).isEqualTo(7L);
        assertThat(event.operationId()).isEqualTo("op-1");
        verify(publisher).publish(event);
    }

    @Test
    void unaPreguntaYaPendienteNoPasaOtraVez() {
        assertThatThrownBy(() -> service.submit(new SubmitQuestionForReview("op-1", 7L, "PENDING_REVIEW")))
                .isInstanceOf(InvalidTransitionException.class);
        verifyNoInteractions(publisher);
    }

    @Test
    void unEstadoDesconocidoFalla() {
        assertThatThrownBy(() -> service.submit(new SubmitQuestionForReview("op-1", 7L, "PUBLICADA")))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(publisher);
    }

    @Test
    void elListenerLlamaAlServicio() {
        new SubmitQuestionListener(service).onSubmit(new SubmitQuestionForReview("op-2", 3L, "DRAFT"));

        verify(publisher).publish(new QuestionStatusChanged(3L, "PENDING_REVIEW", "op-2"));
    }

    @Test
    void elListenerNoReencolaLosMensajesImposibles() {
        var listener = new SubmitQuestionListener(service);

        assertThatThrownBy(() -> listener.onSubmit(new SubmitQuestionForReview("op", 1L, "PENDING_REVIEW")))
                .isInstanceOf(AmqpRejectAndDontRequeueException.class);
        assertThatThrownBy(() -> listener.onSubmit(new SubmitQuestionForReview("op", 1L, "XYZ")))
                .isInstanceOf(AmqpRejectAndDontRequeueException.class);
    }

    @Test
    void elListenerEscuchaLaColaDelEnunciado() throws NoSuchMethodException {
        RabbitListener annotation = SubmitQuestionListener.class
                .getMethod("onSubmit", SubmitQuestionForReview.class).getAnnotation(RabbitListener.class);

        assertThat(annotation).isNotNull();
        assertThat(annotation.queues()).containsExactly("question.submit.review.queue");
    }

    @Test
    void elPublicadorEnviaElAvisoDeEstado() {
        RabbitTemplate template = mock(RabbitTemplate.class);
        var event = new QuestionStatusChanged(3L, "PENDING_REVIEW", "op-2");

        new RabbitStatusChangedPublisher(template).publish(event);

        verify(template).convertAndSend("saberpro.direct.exchange", "question.status.changed", event);
    }
}
