package co.edu.unicauca.history;

import co.edu.unicauca.history.dto.EventEnvelope;
import co.edu.unicauca.history.messaging.HistoryListener;
import co.edu.unicauca.history.model.Entrada;
import co.edu.unicauca.history.service.HistoryService;
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

@SpringBootTest(properties = "spring.rabbitmq.listener.simple.auto-startup=false")
@AutoConfigureMockMvc
class HistoryServiceTest {

    @Autowired
    HistoryListener listener;

    @Autowired
    HistoryService servicio;

    @Autowired
    MockMvc mvc;

    @Test
    void cadaEventoDeUnaPreguntaQuedaEnSuLineaDeTiempoEnOrden() {
        listener.alRecibirUnEvento(EventEnvelope.of("question.state-changed", Map.of("questionId", 10, "from", "BORRADOR", "to", "PENDIENTE_REVISION", "changedBy", "2")));
        listener.alRecibirUnEvento(EventEnvelope.of("ReviewerAssigned", Map.of("questionId", 10, "reviewerId", 4, "round", 1, "assignedBy", 1)));
        listener.alRecibirUnEvento(EventEnvelope.of("ReviewCompleted", Map.of("questionId", 10, "result", "APPROVED", "authorId", 2)));

        List<Entrada> linea = servicio.lineaDeTiempo(10L);
        assertThat(linea).extracting(Entrada::getTipo).containsExactly("question.state-changed", "ReviewerAssigned", "ReviewCompleted");
        assertThat(linea.get(0).getResumen()).isEqualTo("Cambió de BORRADOR a PENDIENTE_REVISION");
        assertThat(linea.get(0).getActor()).isEqualTo("2");
        assertThat(linea.get(1).getActor()).isEqualTo("1");
        assertThat(linea.get(2).getResumen()).contains("APPROVED");
    }

    @Test
    void unEventoRepetidoSeRegistraUnaSolaVez() {
        EventEnvelope evento = EventEnvelope.of("question.published", Map.of("questionId", 11, "authorId", 2));
        listener.alRecibirUnEvento(evento);
        listener.alRecibirUnEvento(evento);
        assertThat(servicio.lineaDeTiempo(11L)).hasSize(1);
    }

    @Test
    void losEventosDeSeguridadNoTienenPreguntaYSeConsultanEnLaAuditoria() throws Exception {
        listener.alRecibirUnEvento(EventEnvelope.of("auth.login-failed", Map.of("username", "autor9")));
        listener.alRecibirUnEvento(EventEnvelope.of("auth.login-succeeded", Map.of("userId", 3, "username", "autor2")));

        mvc.perform(get("/history/audit").param("tipo", "auth.login-failed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].resumen").value("Inicio de sesión fallido de autor9"))
                .andExpect(jsonPath("$.content[0].questionId").doesNotExist());
        assertThat(servicio.auditoria(null, 0, 50).getContent()).extracting(Entrada::getTipo).contains("auth.login-succeeded", "auth.login-failed");
    }

    @Test
    void elEndpointDevuelveLaLineaDeTiempoYUnaPreguntaSinHechosDaListaVacia() throws Exception {
        listener.alRecibirUnEvento(EventEnvelope.of("ValidationFailed", Map.of("questionId", 12, "authorId", 2, "errors", List.of("x"))));
        mvc.perform(get("/history/{id}", 12)).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].tipo").value("ValidationFailed"));
        mvc.perform(get("/history/{id}", 999)).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
    }
}
