package co.edu.unicauca.review;

import co.edu.unicauca.review.client.LifecycleClient;
import co.edu.unicauca.review.client.ServicioException;
import co.edu.unicauca.review.client.UserClient;
import co.edu.unicauca.review.controller.ApiException;
import co.edu.unicauca.review.dto.Dtos.UsuarioInfo;
import co.edu.unicauca.review.dto.EventEnvelope;
import co.edu.unicauca.review.messaging.QuestionSubmittedListener;
import co.edu.unicauca.review.model.CasoDeRevision;
import co.edu.unicauca.review.model.CasoEstado;
import co.edu.unicauca.review.model.Decision;
import co.edu.unicauca.review.service.ReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.rabbitmq.listener.simple.auto-startup=false")
@AutoConfigureMockMvc
class ReviewServiceTest {

    private static final long ADMIN = 1, AUTOR = 2, REV1 = 4, REV2 = 5, REV3 = 6, REV4 = 7;
    private static final AtomicLong PREGUNTAS = new AtomicLong(500);

    @Autowired
    ReviewService servicio;

    @Autowired
    QuestionSubmittedListener listener;

    @Autowired
    MockMvc mvc;

    @MockitoBean
    RabbitTemplate rabbitTemplate;

    @MockitoBean
    LifecycleClient ciclo;

    @MockitoBean
    UserClient usuarios;

    @BeforeEach
    void preparar() {
        reset(rabbitTemplate, ciclo, usuarios);
        when(usuarios.buscar(ADMIN)).thenReturn(Optional.of(new UsuarioInfo(ADMIN, Set.of("ADMINISTRADOR"))));
        when(usuarios.buscar(AUTOR)).thenReturn(Optional.of(new UsuarioInfo(AUTOR, Set.of("AUTOR"))));
        for (long r : new long[]{REV1, REV2, REV3, REV4}) {
            when(usuarios.buscar(r)).thenReturn(Optional.of(new UsuarioInfo(r, Set.of("REVISOR"))));
        }
        when(ciclo.estadoDe(anyLong())).thenReturn("PENDIENTE_REVISION");
    }

    /** Una pregunta enviada a revisión: el evento abre el caso. */
    private long preguntaEnviada() {
        long id = PREGUNTAS.incrementAndGet();
        listener.alEnviarseUnaPregunta(EventEnvelope.of("QuestionSubmittedForReview",
                Map.of("questionId", id, "authorId", AUTOR, "title", "Pregunta " + id, "competency", "Lectura crítica")));
        return id;
    }

    private long preguntaEnRevision(long... revisores) {
        long id = preguntaEnviada();
        servicio.asignar(id, java.util.Arrays.stream(revisores).boxed().toList(), ADMIN);
        return id;
    }

    @Test
    void elEventoDeEnvioAbreElCasoYUnEventoRepetidoNoLoDuplica() {
        long id = PREGUNTAS.incrementAndGet();
        EventEnvelope evento = EventEnvelope.of("QuestionSubmittedForReview", Map.of("questionId", id, "authorId", AUTOR, "title", "T"));
        listener.alEnviarseUnaPregunta(evento);
        listener.alEnviarseUnaPregunta(evento);

        List<CasoDeRevision> rondas = servicio.rondasDe(id);
        assertThat(rondas).hasSize(1);
        assertThat(rondas.get(0).getEstado()).isEqualTo(CasoEstado.PENDIENTE_ASIGNACION);
        assertThat(rondas.get(0).getRonda()).isEqualTo(1);
    }

    @Test
    void asignarRevisoresPideLaTransicionYPublicaReviewerAssigned() {
        long id = preguntaEnviada();
        CasoDeRevision caso = servicio.asignar(id, List.of(REV1, REV2), ADMIN);

        assertThat(caso.getEstado()).isEqualTo(CasoEstado.EN_CURSO);
        assertThat(caso.getAsignaciones()).hasSize(2);
        verify(ciclo).transicionar(id, "EN_REVISION", "1", null);
        verify(rabbitTemplate, org.mockito.Mockito.times(2)).convertAndSend(eq("saberpro.topic.exchange"), eq("reviewer.assigned"), any(Object.class));
    }

    @Test
    void sinRevisoresOConMasDeTresSeRechaza() {
        long id = preguntaEnviada();
        assertThatThrownBy(() -> servicio.asignar(id, List.of(), ADMIN)).isInstanceOf(ApiException.class).hasMessageContaining("entre 1 y 3");
        assertThatThrownBy(() -> servicio.asignar(id, List.of(REV1, REV2, REV3, REV4), ADMIN)).isInstanceOf(ApiException.class);
        verify(ciclo, never()).transicionar(anyLong(), anyString(), anyString(), any());
    }

    @Test
    void elAutorNoPuedeSerRevisorNiSeRepiteNingunRevisor() {
        long id = preguntaEnviada();
        assertThatThrownBy(() -> servicio.asignar(id, List.of(AUTOR), ADMIN)).hasMessageContaining("autor");
        assertThatThrownBy(() -> servicio.asignar(id, List.of(REV1, REV1), ADMIN)).hasMessageContaining("repetir");
    }

    @Test
    void soloUnAdministradorAsignaYSoloRevisoresElegibles() {
        long id = preguntaEnviada();
        assertThatThrownBy(() -> servicio.asignar(id, List.of(REV1), AUTOR)).hasMessageContaining("administrador");
        assertThatThrownBy(() -> servicio.asignar(id, List.of(AUTOR + 100), ADMIN)).hasMessageContaining("elegible");
        assertThatThrownBy(() -> servicio.asignar(id, List.of(ADMIN), ADMIN)).hasMessageContaining("elegible");
    }

    @Test
    void unaPreguntaQueNoEstaPendienteDeRevisionNoEsElegible() {
        long id = preguntaEnviada();
        when(ciclo.estadoDe(id)).thenReturn("BORRADOR");
        assertThatThrownBy(() -> servicio.asignar(id, List.of(REV1), ADMIN)).hasMessageContaining("no es elegible");
        verify(ciclo, never()).transicionar(anyLong(), anyString(), anyString(), any());
    }

    @Test
    void siLifecycleRechazaLaTransicionNoQuedaNingunaAsignacion() {
        long id = preguntaEnviada();
        doThrow(new ServicioException(409, "Transición no permitida")).when(ciclo).transicionar(eq(id), eq("EN_REVISION"), anyString(), any());

        assertThatThrownBy(() -> servicio.asignar(id, List.of(REV1), ADMIN)).isInstanceOf(ServicioException.class);

        CasoDeRevision caso = servicio.rondasDe(id).get(0);
        assertThat(caso.getEstado()).isEqualTo(CasoEstado.PENDIENTE_ASIGNACION);
        assertThat(caso.getAsignaciones()).isEmpty();
        verify(rabbitTemplate, never()).convertAndSend(anyString(), eq("reviewer.assigned"), any(Object.class));
    }

    @Test
    void unRechazoCierraLaRondaComoRechazadaConLaObservacion() {
        long id = preguntaEnRevision(REV1, REV2);
        CasoDeRevision caso = servicio.evaluar(id, REV1, Decision.REJECT, "Falta claridad en el enunciado");

        assertThat(caso.getEstado()).isEqualTo(CasoEstado.CERRADA);
        assertThat(caso.getResultado()).isEqualTo("REJECTED");
        verify(ciclo).transicionar(eq(id), eq("RECHAZADA"), eq("4"), org.mockito.ArgumentMatchers.contains("Falta claridad"));
        verify(rabbitTemplate).convertAndSend(eq("saberpro.topic.exchange"), eq("review.completed"), any(Object.class));
    }

    @Test
    void rechazarSinObservacionEsInvalido() {
        long id = preguntaEnRevision(REV1);
        assertThatThrownBy(() -> servicio.evaluar(id, REV1, Decision.REJECT, " ")).hasMessageContaining("observación");
    }

    @Test
    void laAprobacionRequiereLaUnanimidad() {
        long id = preguntaEnRevision(REV1, REV2);
        CasoDeRevision parcial = servicio.evaluar(id, REV1, Decision.APPROVE, null);
        assertThat(parcial.getEstado()).isEqualTo(CasoEstado.EN_CURSO);
        verify(ciclo, never()).transicionar(eq(id), eq("APROBADA"), anyString(), any());

        CasoDeRevision final_ = servicio.evaluar(id, REV2, Decision.APPROVE, "Bien");
        assertThat(final_.getEstado()).isEqualTo(CasoEstado.CERRADA);
        assertThat(final_.getResultado()).isEqualTo("APPROVED");
        verify(ciclo).transicionar(id, "APROBADA", "5", null);
    }

    @Test
    void unRevisorNoAsignadoNoEvaluaYNadieEvaluaDosVeces() {
        long id = preguntaEnRevision(REV1, REV2);
        assertThatThrownBy(() -> servicio.evaluar(id, REV3, Decision.APPROVE, null)).hasMessageContaining("no está asignado");
        servicio.evaluar(id, REV1, Decision.APPROVE, null);
        assertThatThrownBy(() -> servicio.evaluar(id, REV1, Decision.APPROVE, null)).hasMessageContaining("ya evaluó");
    }

    @Test
    void siLifecycleNoAceptaLaTransicionFinalLaEvaluacionNoSeGuarda() {
        long id = preguntaEnRevision(REV1);
        doThrow(new ServicioException(503, "lifecycle-service no está disponible")).when(ciclo).transicionar(eq(id), eq("APROBADA"), anyString(), any());

        assertThatThrownBy(() -> servicio.evaluar(id, REV1, Decision.APPROVE, null)).isInstanceOf(ServicioException.class);

        CasoDeRevision caso = servicio.rondasDe(id).get(0);
        assertThat(caso.getEstado()).isEqualTo(CasoEstado.EN_CURSO);
        assertThat(caso.getAsignaciones().get(0).getDecision()).isNull();
    }

    @Test
    void despuesDeUnRechazoUnNuevoEnvioAbreLaSiguienteRonda() {
        long id = preguntaEnRevision(REV1);
        servicio.evaluar(id, REV1, Decision.REJECT, "Corregir");
        listener.alEnviarseUnaPregunta(EventEnvelope.of("QuestionSubmittedForReview", Map.of("questionId", id, "authorId", AUTOR, "title", "T")));

        List<CasoDeRevision> rondas = servicio.rondasDe(id);
        assertThat(rondas).extracting(CasoDeRevision::getRonda).containsExactly(1, 2);
        assertThat(rondas.get(1).getEstado()).isEqualTo(CasoEstado.PENDIENTE_ASIGNACION);
    }

    @Test
    void losEndpointsAsignanEvaluanYListanLosPendientesDelRevisor() throws Exception {
        long id = preguntaEnviada();
        mvc.perform(post("/reviews/assign").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"questionId\":" + id + ",\"reviewerIds\":[4,5],\"assignedBy\":1}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("EN_CURSO"))
                .andExpect(jsonPath("$.asignaciones.length()").value(2));
        mvc.perform(get("/reviews/assigned").param("reviewerId", "4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.questionId == " + id + ")]").exists());
        mvc.perform(post("/reviews/{id}/evaluations", id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reviewerId\":4,\"decision\":\"REJECT\",\"observation\":\"No cumple\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultado").value("REJECTED"));
        mvc.perform(get("/reviews/{id}", id)).andExpect(status().isOk()).andExpect(jsonPath("$[0].asignaciones[0].observacion").value("No cumple"));
        mvc.perform(get("/reviews/{id}", 31337)).andExpect(status().isNotFound());
        mvc.perform(post("/reviews/assign").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"questionId\":" + id + ",\"reviewerIds\":[],\"assignedBy\":1}"))
                .andExpect(status().isBadRequest());
    }
}
