package co.edu.unicauca.saberpro.question.application;

import co.edu.unicauca.saberpro.domain.exception.InvalidQuestionException;
import co.edu.unicauca.saberpro.domain.exception.InvalidTransitionException;
import co.edu.unicauca.saberpro.domain.model.AnswerOption;
import co.edu.unicauca.saberpro.domain.model.Question;
import co.edu.unicauca.saberpro.domain.model.QuestionStatus;
import co.edu.unicauca.saberpro.question.TestQuestions;
import co.edu.unicauca.saberpro.question.application.command.CreateQuestionCommand;
import co.edu.unicauca.saberpro.question.application.command.SubmitQuestionForReview;
import co.edu.unicauca.saberpro.question.application.exception.QuestionNotFoundException;
import co.edu.unicauca.saberpro.question.application.exception.QuestionRejectedException;
import co.edu.unicauca.saberpro.question.application.port.IQuestionRepository;
import co.edu.unicauca.saberpro.question.application.port.ISubmitCommandPublisher;
import co.edu.unicauca.saberpro.question.application.port.IValidationClient;
import co.edu.unicauca.saberpro.question.application.port.IValidationClient.ValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuestionServiceTest {

    @Mock
    IQuestionRepository repository;
    @Mock
    IValidationClient validationClient;
    @Mock
    ISubmitCommandPublisher publisher;

    QuestionService service;

    @BeforeEach
    void setUp() {
        service = new QuestionService(repository, validationClient, publisher);
    }

    private CreateQuestionCommand command() {
        return new CreateQuestionCommand("¿Qué es un microkernel?", "Porque el núcleo queda mínimo y los plugins aparte",
                List.of(new AnswerOption("a", true), new AnswerOption("b", false),
                        new AnswerOption("c", false), new AnswerOption("d", false)));
    }

    @Test
    void creaYGuardaCuandoLaValidacionPasa() {
        when(validationClient.validate(any())).thenReturn(new ValidationResult(true, "ok"));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Question q = service.create(command());

        assertThat(q.getStatus()).isEqualTo(QuestionStatus.DRAFT);
        verify(repository).save(any(Question.class));
    }

    @Test
    void siLaValidacionFallaNoGuardaNada() {
        when(validationClient.validate(any())).thenReturn(new ValidationResult(false, "muy corta"));

        assertThatThrownBy(() -> service.create(command()))
                .isInstanceOf(QuestionRejectedException.class)
                .hasMessageContaining("muy corta");
        verify(repository, never()).save(any());
    }

    @Test
    void siLaEstructuraEstaMalNoGuarda() {
        when(validationClient.validate(any())).thenReturn(new ValidationResult(true, "ok"));
        var tresOpciones = new CreateQuestionCommand("enunciado", "justificación suficientemente larga",
                List.of(new AnswerOption("a", true), new AnswerOption("b", false), new AnswerOption("c", false)));

        assertThatThrownBy(() -> service.create(tresOpciones)).isInstanceOf(InvalidQuestionException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void envioPublicaElComandoYDevuelveElOperationId() {
        Question q = new Question(5L, "enunciado", "justificación larga de prueba",
                TestQuestions.draft().getOptions(), QuestionStatus.DRAFT.newState());
        when(repository.findById(5L)).thenReturn(Optional.of(q));

        String operationId = service.submitForReview(5L);

        ArgumentCaptor<SubmitQuestionForReview> captor = ArgumentCaptor.forClass(SubmitQuestionForReview.class);
        verify(publisher).publish(captor.capture());
        assertThat(captor.getValue().operationId()).isEqualTo(operationId);
        assertThat(captor.getValue().questionId()).isEqualTo(5L);
        assertThat(captor.getValue().currentStatus()).isEqualTo("DRAFT");
    }

    @Test
    void noSePuedeEnviarUnaPreguntaYaPendiente() {
        Question pendiente = TestQuestions.draft().withStatus(QuestionStatus.PENDING_REVIEW);
        when(repository.findById(5L)).thenReturn(Optional.of(pendiente));

        assertThatThrownBy(() -> service.submitForReview(5L)).isInstanceOf(InvalidTransitionException.class);
        verifyNoInteractions(publisher);
    }

    @Test
    void applyStatusGuardaLaPreguntaConElEstadoNuevo() {
        when(repository.findById(5L)).thenReturn(Optional.of(TestQuestions.draft()));

        service.applyStatus(5L, QuestionStatus.PENDING_REVIEW);

        ArgumentCaptor<Question> captor = ArgumentCaptor.forClass(Question.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(QuestionStatus.PENDING_REVIEW);
    }

    @Test
    void applyStatusDeUnaPreguntaQueNoExisteFalla() {
        when(repository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.applyStatus(9L, QuestionStatus.PENDING_REVIEW))
                .isInstanceOf(QuestionNotFoundException.class);
    }

    @Test
    void enviarUnaPreguntaQueNoExisteFalla() {
        when(repository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.submitForReview(9L)).isInstanceOf(QuestionNotFoundException.class);
        verifyNoInteractions(publisher);
    }
}
