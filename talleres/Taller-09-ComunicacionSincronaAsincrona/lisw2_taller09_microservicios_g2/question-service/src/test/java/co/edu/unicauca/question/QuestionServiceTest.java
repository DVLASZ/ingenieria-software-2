package co.edu.unicauca.question;

import co.edu.unicauca.question.client.LifecycleClient;
import co.edu.unicauca.question.client.ServicioException;
import co.edu.unicauca.question.client.UserClient;
import co.edu.unicauca.question.client.ValidationClient;
import co.edu.unicauca.question.dto.Dtos.ResultadoValidacion;
import co.edu.unicauca.question.dto.Dtos.UsuarioInfo;
import co.edu.unicauca.question.dto.EventEnvelope;
import co.edu.unicauca.question.messaging.OperationResultListener;
import co.edu.unicauca.question.messaging.StateChangedListener;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.rabbitmq.listener.simple.auto-startup=false")
@AutoConfigureMockMvc
class QuestionServiceTest {

    private static final String PREGUNTA = """
            {"autorId":2,"titulo":"Idea principal","contexto":"Un texto","pregunta":"¿Cuál es la idea principal?",
             "opciones":["Uno","Dos","Tres","Cuatro"],"respuestaCorrecta":"B","justificacion":"Porque sí",
             "bibliografia":"Libro","competencia":"Lectura crítica","tema":"Argumentación","subtema":"Tesis","dificultad":"Básico"}""";

    private static final ObjectMapper JSON = new ObjectMapper();

    @Autowired
    MockMvc mvc;

    @Autowired
    OperationResultListener resultados;

    @Autowired
    StateChangedListener cambios;

    @MockitoBean
    RabbitTemplate rabbitTemplate;

    @MockitoBean
    ValidationClient validacion;

    @MockitoBean
    LifecycleClient ciclo;

    @MockitoBean
    UserClient usuarios;

    @BeforeEach
    void preparar() {
        reset(rabbitTemplate, validacion, ciclo, usuarios);
        when(validacion.validar(any())).thenReturn(new ResultadoValidacion(true, List.of()));
        when(usuarios.buscar(2L)).thenReturn(Optional.of(new UsuarioInfo(2L, Set.of("AUTOR"))));
        when(usuarios.buscar(3L)).thenReturn(Optional.of(new UsuarioInfo(3L, Set.of("AUTOR"))));
        when(usuarios.buscar(4L)).thenReturn(Optional.of(new UsuarioInfo(4L, Set.of("REVISOR"))));
    }

    private long crearPregunta() throws Exception {
        MvcResult r = mvc.perform(post("/questions").header("Authorization", "token").contentType(MediaType.APPLICATION_JSON).content(PREGUNTA))
                .andExpect(status().isCreated()).andReturn();
        return JSON.readTree(r.getResponse().getContentAsString()).get("id").asLong();
    }

    private String enviarARevision(long id) throws Exception {
        MvcResult r = mvc.perform(post("/questions/{id}/submit", id).header("Authorization", "token")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"requestedBy\":\"2\"}"))
                .andExpect(status().isAccepted()).andReturn();
        return JSON.readTree(r.getResponse().getContentAsString()).get("operationId").asText();
    }

    @Test
    void crearUnaPreguntaValidaResponde201EnBorradorYCreaElEstadoEnLifecycle() throws Exception {
        mvc.perform(post("/questions").header("Authorization", "token").contentType(MediaType.APPLICATION_JSON).content(PREGUNTA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("BORRADOR"))
                .andExpect(jsonPath("$.equivalente").value("DRAFT"))
                .andExpect(jsonPath("$.respuestaCorrecta").value("B"));
        verify(ciclo).crearEstado(anyLong(), eq(2L), eq("Idea principal"), eq("Lectura crítica"));
    }

    @Test
    void soloUnAutorPuedeCrearPreguntas() throws Exception {
        mvc.perform(post("/questions").header("Authorization", "token").contentType(MediaType.APPLICATION_JSON)
                        .content(PREGUNTA.replace("\"autorId\":2", "\"autorId\":4")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("no es un autor")));
        mvc.perform(post("/questions").header("Authorization", "token").contentType(MediaType.APPLICATION_JSON)
                        .content(PREGUNTA.replace("\"autorId\":2", "\"autorId\":99")))
                .andExpect(status().isForbidden());
        verify(validacion, never()).validar(any());
    }

    @Test
    void siUserServiceNoEstaDisponibleSeResponde503() throws Exception {
        when(usuarios.buscar(2L)).thenThrow(new ServicioException(503, "user-service no está disponible"));
        mvc.perform(post("/questions").header("Authorization", "token").contentType(MediaType.APPLICATION_JSON).content(PREGUNTA))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    void sinElEncabezadoAuthorizationResponde401() throws Exception {
        mvc.perform(post("/questions").contentType(MediaType.APPLICATION_JSON).content(PREGUNTA))
                .andExpect(status().isUnauthorized());
        verify(validacion, never()).validar(any());
    }

    @Test
    void unaPreguntaInvalidaResponde400ConLosErroresYNoSeRegistra() throws Exception {
        when(validacion.validar(any())).thenReturn(new ResultadoValidacion(false, List.of("Deben ser exactamente cuatro opciones")));
        long antes = total();
        mvc.perform(post("/questions").header("Authorization", "token").contentType(MediaType.APPLICATION_JSON).content(PREGUNTA))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0]").value("Deben ser exactamente cuatro opciones"));
        assertThat(total()).isEqualTo(antes);
        verify(ciclo, never()).crearEstado(anyLong(), anyLong(), anyString(), anyString());
    }

    @Test
    void siLifecycleFallaLaPreguntaNoQuedaRegistrada() throws Exception {
        doThrow(new ServicioException(503, "lifecycle-service no está disponible")).when(ciclo).crearEstado(anyLong(), anyLong(), any(), any());
        long antes = total();
        mvc.perform(post("/questions").header("Authorization", "token").contentType(MediaType.APPLICATION_JSON).content(PREGUNTA))
                .andExpect(status().isServiceUnavailable());
        assertThat(total()).isEqualTo(antes);
    }

    @Test
    void enviarARevisionResponde202PublicaElComandoYLaOperacionQuedaPendiente() throws Exception {
        long id = crearPregunta();
        String operacion = enviarARevision(id);

        ArgumentCaptor<Object> mensaje = ArgumentCaptor.forClass(Object.class);
        verify(rabbitTemplate).convertAndSend(eq("saberpro.direct.exchange"), eq("question.submit"), mensaje.capture());
        EventEnvelope comando = (EventEnvelope) mensaje.getValue();
        assertThat(comando.type()).isEqualTo("SubmitQuestionForReview");
        assertThat(comando.stringOf("operationId")).isEqualTo(operacion);
        assertThat(comando.longOf("questionId")).isEqualTo(id);
        assertThat(comando.stringOf("requestedAt")).isNotBlank();
        mvc.perform(get("/operations/{id}", operacion)).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void elResultadoQueLlegaPorRabbitMQActualizaLaOperacion() throws Exception {
        long id = crearPregunta();
        String operacion = enviarARevision(id);

        resultados.alRecibirElResultado(EventEnvelope.of("OperationResult",
                Map.of("operationId", operacion, "questionId", id, "status", "FAILED", "message", "Transición no permitida")));

        mvc.perform(get("/operations/{id}", operacion)).andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FAILED"))
                .andExpect(jsonPath("$.mensaje").value("Transición no permitida"));
    }

    @Test
    void siRabbitMQNoEstaDisponibleSeRespondeUnErrorYLaOperacionFalla() throws Exception {
        long id = crearPregunta();
        doThrow(new AmqpConnectException(new IOException("sin broker"))).when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class));
        mvc.perform(post("/questions/{id}/submit", id).header("Authorization", "token"))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    void enviarUnaPreguntaInexistenteEs404() throws Exception {
        mvc.perform(post("/questions/{id}/submit", 99999).header("Authorization", "token")).andExpect(status().isNotFound());
    }

    @Test
    void elEventoDeCambioDeEstadoActualizaLaCopiaYElListadoLoFiltra() throws Exception {
        long id = crearPregunta();
        cambios.alCambiarElEstado(EventEnvelope.of("question.state-changed", Map.of("questionId", id, "from", "BORRADOR", "to", "PENDIENTE_REVISION")));

        mvc.perform(get("/questions/{id}", id)).andExpect(jsonPath("$.estado").value("PENDIENTE_REVISION"));
        mvc.perform(get("/questions").param("estado", "PENDIENTE_REVISION").param("competencia", "Lectura crítica"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(id));
    }

    @Test
    void soloSeEditaEnBorradorOEnRechazada() throws Exception {
        long id = crearPregunta();
        cambios.alCambiarElEstado(EventEnvelope.of("question.state-changed", Map.of("questionId", id, "to", "EN_REVISION")));
        mvc.perform(put("/questions/{id}", id).header("Authorization", "token").contentType(MediaType.APPLICATION_JSON).content(PREGUNTA))
                .andExpect(status().isConflict());
    }

    @Test
    void corregirUnaRechazadaLaDevuelveABorradorPidiendoLaTransicionYSubeLaVersion() throws Exception {
        long id = crearPregunta();
        cambios.alCambiarElEstado(EventEnvelope.of("question.state-changed", Map.of("questionId", id, "to", "RECHAZADA")));

        mvc.perform(put("/questions/{id}", id).header("Authorization", "token").contentType(MediaType.APPLICATION_JSON)
                        .content(PREGUNTA.replace("Idea principal", "Idea principal corregida")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("BORRADOR"))
                .andExpect(jsonPath("$.version").value(2))
                .andExpect(jsonPath("$.titulo").value("Idea principal corregida"));
        verify(ciclo).transicionar(eq(id), eq("BORRADOR"), eq("2"), anyString());
    }

    @Test
    void soloElAutorPuedeEditarSuPregunta() throws Exception {
        long id = crearPregunta();
        mvc.perform(put("/questions/{id}", id).header("Authorization", "token").contentType(MediaType.APPLICATION_JSON)
                        .content(PREGUNTA.replace("\"autorId\":2", "\"autorId\":3")))
                .andExpect(status().isForbidden());
    }

    private long total() throws Exception {
        MvcResult r = mvc.perform(get("/questions").param("size", "1")).andReturn();
        JsonNode nodo = JSON.readTree(r.getResponse().getContentAsString());
        return nodo.get("totalElements").asLong();
    }
}
