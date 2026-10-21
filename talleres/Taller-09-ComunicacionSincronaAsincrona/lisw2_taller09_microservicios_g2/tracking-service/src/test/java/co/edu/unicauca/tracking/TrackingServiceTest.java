package co.edu.unicauca.tracking;

import co.edu.unicauca.tracking.dto.EventEnvelope;
import co.edu.unicauca.tracking.messaging.SimulationResultListener;
import co.edu.unicauca.tracking.service.TrackingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.rabbitmq.listener.simple.auto-startup=false")
@AutoConfigureMockMvc
class TrackingServiceTest {

    @Autowired
    SimulationResultListener listener;

    @Autowired
    TrackingService servicio;

    @Autowired
    MockMvc mvc;

    private EventEnvelope simulacro(long usuario, int correctas, int total, double puntaje) {
        return EventEnvelope.of("SimulationCompleted", Map.of("simulationId", 1, "userId", usuario, "correct", correctas, "total", total, "score", puntaje));
    }

    @Test
    void losSimulacrosSeAcumulanPorUsuario() throws Exception {
        listener.alResolverseUnSimulacro(simulacro(101, 1, 2, 50.0));
        listener.alResolverseUnSimulacro(simulacro(101, 2, 2, 100.0));

        mvc.perform(get("/tracking/users/101")).andExpect(status().isOk())
                .andExpect(jsonPath("$.simulacros").value(2))
                .andExpect(jsonPath("$.promedio").value(75.0))
                .andExpect(jsonPath("$.correctas").value(3))
                .andExpect(jsonPath("$.preguntas").value(4))
                .andExpect(jsonPath("$.acierto").value(75.0));
    }

    @Test
    void unEventoRepetidoNoSumaDosVeces() {
        EventEnvelope evento = simulacro(102, 1, 1, 100.0);
        listener.alResolverseUnSimulacro(evento);
        listener.alResolverseUnSimulacro(evento);
        assertThat(servicio.deUsuario(102L).getSimulacros()).isEqualTo(1);
    }

    @Test
    void losEventosAjenosOIncompletosSeIgnoran() {
        listener.alResolverseUnSimulacro(EventEnvelope.of("question.published", Map.of("questionId", 1)));
        listener.alResolverseUnSimulacro(EventEnvelope.of("SimulationCompleted", Map.of("userId", 103)));
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> servicio.deUsuario(103L)).hasMessageContaining("no tiene simulacros");
    }

    @Test
    void elResumenPromediaTodosLosSimulacros() throws Exception {
        listener.alResolverseUnSimulacro(simulacro(104, 1, 1, 100.0));
        mvc.perform(get("/tracking/summary")).andExpect(status().isOk())
                .andExpect(jsonPath("$.usuarios").isNumber())
                .andExpect(jsonPath("$.simulacros").isNumber());
        mvc.perform(get("/tracking/users/9999")).andExpect(status().isNotFound());
    }
}
