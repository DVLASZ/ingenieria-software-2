package co.edu.unicauca.notification;

import co.edu.unicauca.notification.dto.EventEnvelope;
import co.edu.unicauca.notification.messaging.NotificationListener;
import co.edu.unicauca.notification.model.Notificacion;
import co.edu.unicauca.notification.model.NotificationType;
import co.edu.unicauca.notification.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {"spring.rabbitmq.listener.simple.auto-startup=false", "app.notifications.delay-ms=0"})
@AutoConfigureMockMvc
class NotificationServiceTest {

    @Autowired
    NotificationListener listener;

    @Autowired
    NotificationService servicio;

    @Autowired
    MockMvc mvc;

    @Test
    void reviewerAssignedAvisaACadaRevisor() {
        listener.alAsignarseUnRevisor(EventEnvelope.of("ReviewerAssigned",
                Map.of("questionId", 1, "title", "Idea principal", "round", 1, "reviewerId", 4, "assignedBy", 1)));
        listener.alAsignarseUnRevisor(EventEnvelope.of("ReviewerAssigned",
                Map.of("questionId", 1, "title", "Idea principal", "round", 1, "reviewerId", 5, "assignedBy", 1)));

        List<Notificacion> avisos = servicio.buscar(null, NotificationType.REVIEWER_ASSIGNED).stream()
                .filter(n -> n.getQuestionId() == 1L).toList();
        assertThat(avisos).extracting(Notificacion::getDestinatarioId).containsExactlyInAnyOrder(4L, 5L);
        assertThat(avisos).allMatch(n -> n.getMensaje().contains("Idea principal") && n.getEstado().equals("ENVIADA"));
    }

    @Test
    void unEventoRepetidoNoNotificaDosVeces() {
        EventEnvelope evento = EventEnvelope.of("ReviewerAssigned", Map.of("questionId", 2, "title", "T", "round", 1, "reviewerId", 6));
        listener.alAsignarseUnRevisor(evento);
        listener.alAsignarseUnRevisor(evento);
        assertThat(servicio.buscar(6L, NotificationType.REVIEWER_ASSIGNED).stream().filter(n -> n.getQuestionId() == 2L)).hasSize(1);
    }

    @Test
    void laEvaluacionAvisaAlAutorYAlAdministrador() {
        listener.alRecibirUnEvento(EventEnvelope.of("EvaluationSubmitted",
                Map.of("questionId", 3, "title", "T", "authorId", 12, "adminId", 11, "reviewerId", 4, "decision", "APPROVE")));
        assertThat(servicio.buscar(12L, NotificationType.EVALUATION_SUBMITTED)).hasSize(1);
        assertThat(servicio.buscar(11L, NotificationType.EVALUATION_SUBMITTED)).hasSize(1);
    }

    @Test
    void elResultadoFinalAvisaALosDosYDiceSiFueAprobadaORechazada() {
        listener.alRecibirUnEvento(EventEnvelope.of("ReviewCompleted",
                Map.of("questionId", 4, "title", "T", "authorId", 22, "adminId", 21, "result", "REJECTED")));
        assertThat(servicio.buscar(22L, NotificationType.QUESTION_REJECTED)).singleElement()
                .satisfies(n -> assertThat(n.getMensaje()).contains("rechazada"));
        assertThat(servicio.buscar(21L, NotificationType.QUESTION_REJECTED)).hasSize(1);
        listener.alRecibirUnEvento(EventEnvelope.of("ReviewCompleted",
                Map.of("questionId", 14, "title", "T", "authorId", 23, "adminId", 21, "result", "APPROVED")));
        assertThat(servicio.buscar(23L, NotificationType.QUESTION_APPROVED)).hasSize(1);
    }

    @Test
    void laValidacionFallidaYLaPublicacionAvisanAlAutor() {
        listener.alRecibirUnEvento(EventEnvelope.of("ValidationFailed", Map.of("title", "T", "authorId", 32, "errors", List.of("x"))));
        listener.alRecibirUnEvento(EventEnvelope.of("question.published", Map.of("questionId", 5, "title", "T", "authorId", 32)));
        assertThat(servicio.buscar(32L, null)).extracting(Notificacion::getTipo)
                .containsExactlyInAnyOrder(NotificationType.VALIDATION_FAILED, NotificationType.QUESTION_PUBLISHED);
    }

    @Test
    void losEventosSinNotificacionSeIgnoran() {
        long antes = servicio.buscar(null, null).size();
        listener.alRecibirUnEvento(EventEnvelope.of("question.state-changed", Map.of("questionId", 6)));
        assertThat(servicio.buscar(null, null)).hasSize((int) antes);
    }

    @Test
    void elEndpointListaLasNotificacionesDeUnDestinatario() throws Exception {
        listener.alRecibirUnEvento(EventEnvelope.of("question.published", Map.of("questionId", 7, "title", "T", "authorId", 42)));
        mvc.perform(get("/notifications").param("destinatario", "42").param("tipo", "QUESTION_PUBLISHED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].asunto").value("Tu pregunta fue publicada"));
    }
}
