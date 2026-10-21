package co.edu.unicauca.simulation;

import co.edu.unicauca.simulation.client.QuestionClient;
import co.edu.unicauca.simulation.dto.Dtos.PreguntaRemota;
import co.edu.unicauca.simulation.dto.Dtos.RespuestaItem;
import co.edu.unicauca.simulation.dto.EventEnvelope;
import co.edu.unicauca.simulation.messaging.PublishedQuestionListener;
import co.edu.unicauca.simulation.model.Simulacro;
import co.edu.unicauca.simulation.service.SimulationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.rabbitmq.listener.simple.auto-startup=false")
@AutoConfigureMockMvc
class SimulationServiceTest {

    @Autowired
    PublishedQuestionListener listener;

    @Autowired
    SimulationService servicio;

    @Autowired
    MockMvc mvc;

    @MockitoBean
    RabbitTemplate rabbitTemplate;

    @MockitoBean
    QuestionClient preguntas;

    @BeforeEach
    void preparar() {
        reset(rabbitTemplate, preguntas);
        when(preguntas.obtener(1L)).thenReturn(new PreguntaRemota(1L, "Primera", "ctx", "¿Uno?", List.of("a", "b", "c", "d"), "A", "Lectura crítica"));
        when(preguntas.obtener(2L)).thenReturn(new PreguntaRemota(2L, "Segunda", "ctx", "¿Dos?", List.of("a", "b", "c", "d"), "C", "Inglés"));
        when(preguntas.obtener(50L)).thenReturn(new PreguntaRemota(50L, "Aparte", "ctx", "¿Cincuenta?", List.of("a", "b", "c", "d"), "D", "Inglés"));
    }

    private void publicar(long id) {
        listener.alPublicarseUnaPregunta(EventEnvelope.of("question.published", Map.of("questionId", id, "title", "T")));
    }

    @Test
    void laPreguntaPublicadaEntraAlCatalogoUnaSolaVezYSinMostrarLaRespuesta() throws Exception {
        publicar(50);
        publicar(50);
        verify(preguntas, times(1)).obtener(50L);
        assertThat(servicio.catalogo()).extracting("questionId").contains(50L);
        mvc.perform(get("/simulations/catalog")).andExpect(status().isOk()).andExpect(jsonPath("$[0].respuestaCorrecta").doesNotExist());
    }

    @Test
    void unSimulacroSeResuelveSeCalificaYPublicaSimulationCompleted() {
        publicar(1);
        publicar(2);
        Simulacro s = servicio.crear(7L, 2);
        Simulacro resuelto = servicio.resolver(s.getId(), List.of(new RespuestaItem(1L, "a"), new RespuestaItem(2L, "B")));

        assertThat(resuelto.getCorrectas()).isEqualTo(1);
        assertThat(resuelto.getTotal()).isEqualTo(2);
        assertThat(resuelto.getPuntaje()).isEqualTo(50.0);
        ArgumentCaptor<Object> mensaje = ArgumentCaptor.forClass(Object.class);
        verify(rabbitTemplate).convertAndSend(eq("saberpro.topic.exchange"), eq("simulation.completed"), mensaje.capture());
        EventEnvelope evento = (EventEnvelope) mensaje.getValue();
        assertThat(evento.type()).isEqualTo("SimulationCompleted");
        assertThat(evento.longOf("userId")).isEqualTo(7L);
        assertThat(evento.data().get("score")).isEqualTo(50.0);
    }

    @Test
    void lasRespuestasQueFaltanCuentanComoIncorrectas() {
        publicar(1);
        Simulacro s = servicio.crear(7L, 1);
        assertThat(servicio.resolver(s.getId(), List.of()).getPuntaje()).isEqualTo(0.0);
    }

    @Test
    void noSePuedeResolverDosVecesNiConPreguntasAjenas() {
        publicar(1);
        Simulacro s = servicio.crear(7L, 1);
        assertThatThrownBy(() -> servicio.resolver(s.getId(), List.of(new RespuestaItem(99L, "A")))).hasMessageContaining("no pertenecen");
        servicio.resolver(s.getId(), List.of(new RespuestaItem(1L, "A")));
        assertThatThrownBy(() -> servicio.resolver(s.getId(), List.of())).hasMessageContaining("ya fue resuelto");
    }

    @Test
    void noSePideMasPreguntasDeLasQueTieneElCatalogo() throws Exception {
        publicar(1);
        int disponibles = servicio.catalogo().size();
        mvc.perform(post("/simulations").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":7,\"cantidad\":" + (disponibles + 5) + "}"))
                .andExpect(status().isConflict());
        mvc.perform(post("/simulations").contentType(MediaType.APPLICATION_JSON).content("{\"userId\":7,\"cantidad\":0}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void elEndpointCreaYResuelveUnSimulacro() throws Exception {
        publicar(1);
        var creado = mvc.perform(post("/simulations").contentType(MediaType.APPLICATION_JSON).content("{\"userId\":8,\"cantidad\":1}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.preguntas[0].respuestaCorrecta").doesNotExist())
                .andReturn();
        int id = new com.fasterxml.jackson.databind.ObjectMapper().readTree(creado.getResponse().getContentAsString()).get("id").asInt();
        mvc.perform(post("/simulations/{id}/submit", id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"respuestas\":[{\"questionId\":1,\"respuesta\":\"A\"}]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.puntaje").value(100.0));
        mvc.perform(post("/simulations/{id}/submit", 4040).contentType(MediaType.APPLICATION_JSON).content("{\"respuestas\":[]}"))
                .andExpect(status().isNotFound());
    }
}
