package co.edu.unicauca.lifecycle;

import co.edu.unicauca.lifecycle.controller.ApiException;
import co.edu.unicauca.lifecycle.dto.EventEnvelope;
import co.edu.unicauca.lifecycle.model.EstadoPregunta;
import co.edu.unicauca.lifecycle.model.OperationStatus;
import co.edu.unicauca.lifecycle.service.LifecycleService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.rabbitmq.listener.simple.auto-startup=false")
@AutoConfigureMockMvc
class LifecycleServiceTest {

    private static final AtomicLong IDS = new AtomicLong(100);

    @Autowired
    LifecycleService servicio;

    @Autowired
    MockMvc mvc;

    @MockitoBean
    RabbitTemplate rabbitTemplate;

    private long preguntaNueva() {
        long id = IDS.incrementAndGet();
        servicio.crear(id, 2L, "Pregunta " + id, "Lectura crítica");
        return id;
    }

    private EventEnvelope comando(String tipo, long questionId, String operationId) {
        return EventEnvelope.of(tipo, Map.of("questionId", questionId, "operationId", operationId, "requestedBy", "2"));
    }

    @Test
    void unaPreguntaNuevaNaceEnBorradorYPublicaElCambio() {
        long id = preguntaNueva();
        assertThat(servicio.obtener(id).getEstado()).isEqualTo(EstadoPregunta.BORRADOR);
        verify(rabbitTemplate, atLeastOnce()).convertAndSend(eq("saberpro.topic.exchange"), eq("question.state-changed"), any(Object.class));
    }

    @Test
    void noSePuedeCrearDosVecesElEstadoDeUnaPregunta() {
        long id = preguntaNueva();
        assertThatThrownBy(() -> servicio.crear(id, 2L, "x", "y")).isInstanceOf(ApiException.class);
    }

    @Test
    void unaTransicionValidaCambiaElEstadoYAnunciaLaPreguntaEnviada() {
        long id = preguntaNueva();
        servicio.transicionar(id, EstadoPregunta.PENDIENTE_REVISION, "2", null);
        assertThat(servicio.obtener(id).getEstado()).isEqualTo(EstadoPregunta.PENDIENTE_REVISION);
        verify(rabbitTemplate).convertAndSend(eq("saberpro.topic.exchange"), eq("question.submitted"), any(Object.class));
    }

    @Test
    void unaTransicionInvalidaEsRechazada() {
        long id = preguntaNueva();
        assertThatThrownBy(() -> servicio.transicionar(id, EstadoPregunta.APROBADA, "4", null))
                .isInstanceOf(ApiException.class).hasMessageContaining("Transición no permitida");
        assertThat(servicio.obtener(id).getEstado()).isEqualTo(EstadoPregunta.BORRADOR);
    }

    @Test
    void rechazarExigeUnMotivo() {
        long id = preguntaNueva();
        servicio.transicionar(id, EstadoPregunta.PENDIENTE_REVISION, "2", null);
        servicio.transicionar(id, EstadoPregunta.EN_REVISION, "1", null);
        assertThatThrownBy(() -> servicio.transicionar(id, EstadoPregunta.RECHAZADA, "4", " "))
                .isInstanceOf(ApiException.class).hasMessageContaining("motivo");
        servicio.transicionar(id, EstadoPregunta.RECHAZADA, "4", "Falta claridad");
        assertThat(servicio.historialDe(id)).last().extracting("motivo").isEqualTo("Falta claridad");
    }

    @Test
    void elComandoDeEnvioCompletaLaOperacionYPublicaElResultado() {
        long id = preguntaNueva();
        servicio.procesarComando(comando("SubmitQuestionForReview", id, "op-" + id));

        assertThat(servicio.operacion("op-" + id).getStatus()).isEqualTo(OperationStatus.COMPLETED);
        assertThat(servicio.obtener(id).getEstado()).isEqualTo(EstadoPregunta.PENDIENTE_REVISION);
        ArgumentCaptor<Object> mensaje = ArgumentCaptor.forClass(Object.class);
        verify(rabbitTemplate).convertAndSend(eq("saberpro.direct.exchange"), eq("question.operation-result"), mensaje.capture());
        assertThat(((EventEnvelope) mensaje.getValue()).stringOf("status")).isEqualTo("COMPLETED");
    }

    @Test
    void unComandoInvalidoQuedaFailedConElMotivoYNoCambiaElEstado() {
        long id = preguntaNueva();
        servicio.procesarComando(comando("SubmitQuestionForReview", id, "a-" + id));
        servicio.procesarComando(comando("SubmitQuestionForReview", id, "b-" + id));

        var segunda = servicio.operacion("b-" + id);
        assertThat(segunda.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(segunda.getMensaje()).contains("Pendiente de revisión");
        assertThat(servicio.obtener(id).getEstado()).isEqualTo(EstadoPregunta.PENDIENTE_REVISION);
    }

    @Test
    void unComandoDeUnaPreguntaInexistenteQuedaFailed() {
        servicio.procesarComando(comando("PublishQuestion", 987654L, "op-x"));
        assertThat(servicio.operacion("op-x").getStatus()).isEqualTo(OperationStatus.FAILED);
    }

    @Test
    void elMismoComandoDosVecesSeProcesaUnaSolaVez() {
        long id = preguntaNueva();
        servicio.procesarComando(comando("SubmitQuestionForReview", id, "dup-" + id));
        servicio.procesarComando(comando("SubmitQuestionForReview", id, "dup-" + id));
        assertThat(servicio.operacion("dup-" + id).getStatus()).isEqualTo(OperationStatus.COMPLETED);
        assertThat(servicio.historialDe(id)).hasSize(2);
    }

    @Test
    void laRutaDeLaGuiaTambienTransiciona() throws Exception {
        long id = preguntaNueva();
        mvc.perform(post("/lifecycle/transition").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"questionId\":" + id + ",\"to\":\"PENDIENTE_REVISION\",\"requestedBy\":\"2\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.estado").value("PENDIENTE_REVISION"));
        mvc.perform(post("/lifecycle/transition").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"questionId\":" + id + ",\"to\":\"APROBADA\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void elEventoDePreguntaEnviadaLlevaLaOperacionYElNuevoEstado() {
        long id = preguntaNueva();
        servicio.procesarComando(comando("SubmitQuestionForReview", id, "ev-" + id));
        ArgumentCaptor<Object> mensaje = ArgumentCaptor.forClass(Object.class);
        verify(rabbitTemplate).convertAndSend(eq("saberpro.topic.exchange"), eq("question.submitted"), mensaje.capture());
        EventEnvelope evento = (EventEnvelope) mensaje.getValue();
        assertThat(evento.stringOf("operationId")).isEqualTo("ev-" + id);
        assertThat(evento.stringOf("newState")).isEqualTo("PENDIENTE_REVISION");
    }

    @Test
    void elEndpointDeTransicionResponde200Y409() throws Exception {
        long id = preguntaNueva();
        mvc.perform(post("/lifecycle/{id}/transition", id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"to\":\"PENDIENTE_REVISION\",\"requestedBy\":\"2\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("PENDIENTE_REVISION"))
                .andExpect(jsonPath("$.equivalente").value("PENDING_REVIEW"));
        mvc.perform(post("/lifecycle/{id}/transition", id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"to\":\"APROBADA\",\"requestedBy\":\"4\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Transición no permitida")));
        mvc.perform(get("/lifecycle/{id}", id)).andExpect(status().isOk()).andExpect(jsonPath("$.historial.length()").value(2));
        mvc.perform(get("/lifecycle/{id}", 424242)).andExpect(status().isNotFound());
    }
}
