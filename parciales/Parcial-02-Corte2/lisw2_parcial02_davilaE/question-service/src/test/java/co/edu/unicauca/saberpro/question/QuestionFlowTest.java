package co.edu.unicauca.saberpro.question;

import co.edu.unicauca.saberpro.domain.model.Question;
import co.edu.unicauca.saberpro.domain.model.QuestionStatus;
import co.edu.unicauca.saberpro.question.application.command.SubmitQuestionForReview;
import co.edu.unicauca.saberpro.question.application.event.QuestionStatusChanged;
import co.edu.unicauca.saberpro.question.infrastructure.messaging.QuestionStatusChangedListener;
import co.edu.unicauca.saberpro.question.application.port.IQuestionRepository;
import co.edu.unicauca.saberpro.question.application.port.IValidationClient;
import co.edu.unicauca.saberpro.question.application.port.IValidationClient.ValidationResult;
import co.edu.unicauca.saberpro.question.infrastructure.jpa.SpringQuestionDataRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** el servicio completo con H2 real; solo se falsean validation-service y RabbitMQ */
@SpringBootTest(properties = "spring.rabbitmq.listener.simple.auto-startup=false")
@AutoConfigureMockMvc
class QuestionFlowTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    IQuestionRepository repository;

    @Autowired
    SpringQuestionDataRepository dataRepository;

    @Autowired
    QuestionStatusChangedListener listener;

    @MockitoBean
    IValidationClient validationClient;

    @MockitoBean
    RabbitTemplate rabbitTemplate;

    @Test
    void creaGuardaEnH2YPublicaElEnvio() throws Exception {
        when(validationClient.validate(any())).thenReturn(new ValidationResult(true, "ok"));

        mvc.perform(post("/questions").contentType(MediaType.APPLICATION_JSON).content(TestQuestions.VALID_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("DRAFT"));

        Long id = dataRepository.findAll().get(dataRepository.findAll().size() - 1).getId();
        Question guardada = repository.findById(id).orElseThrow();
        assertThat(guardada.getStatus()).isEqualTo(QuestionStatus.DRAFT);
        assertThat(guardada.getOptions()).hasSize(4);

        mvc.perform(post("/questions/" + id + "/submit"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.operationId").isNotEmpty());

        ArgumentCaptor<SubmitQuestionForReview> captor = ArgumentCaptor.forClass(SubmitQuestionForReview.class);
        verify(rabbitTemplate).convertAndSend(eq("saberpro.direct.exchange"), eq("question.submit"), captor.capture());
        assertThat(captor.getValue().questionId()).isEqualTo(id);
        assertThat(captor.getValue().currentStatus()).isEqualTo("DRAFT");

        // hasta que lifecycle-service avise, la pregunta sigue en DRAFT
        assertThat(repository.findById(id).orElseThrow().getStatus()).isEqualTo(QuestionStatus.DRAFT);

        listener.onStatusChanged(new QuestionStatusChanged(id, "PENDING_REVIEW", "op-1"));

        assertThat(repository.findById(id).orElseThrow().getStatus()).isEqualTo(QuestionStatus.PENDING_REVIEW);
        assertThat(repository.findById(id).orElseThrow().getOptions()).hasSize(4);
        mvc.perform(post("/questions/" + id + "/submit")).andExpect(status().isConflict());
    }

    @Test
    void laValidacionFallidaDa400YNoGuarda() throws Exception {
        long antes = dataRepository.count();
        when(validationClient.validate(any())).thenReturn(new ValidationResult(false, "muy corta"));

        mvc.perform(post("/questions").contentType(MediaType.APPLICATION_JSON).content(TestQuestions.VALID_JSON))
                .andExpect(status().isBadRequest());

        assertThat(dataRepository.count()).isEqualTo(antes);
        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), any(Object.class));
    }

    @Test
    void submitDeUnaPreguntaQueNoExisteDa404() throws Exception {
        mvc.perform(post("/questions/99999/submit")).andExpect(status().isNotFound());
        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), any(Object.class));
    }
}
