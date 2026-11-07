package co.edu.unicauca.saberpro.question.api;

import co.edu.unicauca.saberpro.domain.exception.InvalidTransitionException;
import co.edu.unicauca.saberpro.domain.model.Question;
import co.edu.unicauca.saberpro.domain.model.QuestionStatus;
import co.edu.unicauca.saberpro.question.TestQuestions;
import co.edu.unicauca.saberpro.question.application.QuestionService;
import co.edu.unicauca.saberpro.question.application.exception.MessagingUnavailableException;
import co.edu.unicauca.saberpro.question.application.exception.QuestionNotFoundException;
import co.edu.unicauca.saberpro.question.application.exception.QuestionRejectedException;
import co.edu.unicauca.saberpro.question.application.exception.ValidationServiceUnavailableException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(QuestionController.class)
class QuestionControllerTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    QuestionService service;

    private Question saved() {
        return new Question(1L, "enunciado", "justificación larga de prueba",
                TestQuestions.draft().getOptions(), QuestionStatus.DRAFT.newState());
    }

    @Test
    void creaYResponde201() throws Exception {
        when(service.create(any())).thenReturn(saved());

        mvc.perform(post("/questions").contentType(MediaType.APPLICATION_JSON).content(TestQuestions.VALID_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.options.length()").value(4));
    }

    @Test
    void responde400SiLaValidacionRechaza() throws Exception {
        when(service.create(any())).thenThrow(new QuestionRejectedException("muy corta"));

        mvc.perform(post("/questions").contentType(MediaType.APPLICATION_JSON).content(TestQuestions.VALID_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details[0]").value("muy corta"));
    }

    @Test
    void responde400SiElJsonEstaRoto() throws Exception {
        mvc.perform(post("/questions").contentType(MediaType.APPLICATION_JSON).content("{no es json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void responde503SiValidationNoResponde() throws Exception {
        when(service.create(any())).thenThrow(new ValidationServiceUnavailableException("caído", null));

        mvc.perform(post("/questions").contentType(MediaType.APPLICATION_JSON).content(TestQuestions.VALID_JSON))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    void submitResponde202ConOperationId() throws Exception {
        when(service.submitForReview(1L)).thenReturn("op-123");

        mvc.perform(post("/questions/1/submit"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.operationId").value("op-123"))
                .andExpect(jsonPath("$.questionId").value(1));
    }

    @Test
    void submitDeUnaPreguntaQueNoExisteDa404() throws Exception {
        when(service.submitForReview(9L)).thenThrow(new QuestionNotFoundException(9L));

        mvc.perform(post("/questions/9/submit")).andExpect(status().isNotFound());
    }

    @Test
    void submitDeUnaPreguntaYaPendienteDa409() throws Exception {
        when(service.submitForReview(1L)).thenThrow(new InvalidTransitionException("ya está pendiente"));

        mvc.perform(post("/questions/1/submit")).andExpect(status().isConflict());
    }

    @Test
    void submitSinRabbitDa503() throws Exception {
        when(service.submitForReview(1L)).thenThrow(new MessagingUnavailableException("RabbitMQ caído", null));

        mvc.perform(post("/questions/1/submit")).andExpect(status().isServiceUnavailable());
    }
}
