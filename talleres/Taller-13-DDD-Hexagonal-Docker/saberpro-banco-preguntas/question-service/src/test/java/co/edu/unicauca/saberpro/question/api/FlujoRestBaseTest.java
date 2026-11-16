package co.edu.unicauca.saberpro.question.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.edu.unicauca.saberpro.question.Fakes.NotificacionFake;
import co.edu.unicauca.saberpro.question.Fakes.ValidacionFake;
import co.edu.unicauca.saberpro.question.domain.model.EstadoPregunta;
import co.edu.unicauca.saberpro.question.domain.ports.inbound.EvaluarPreguntaUseCase;
import co.edu.unicauca.saberpro.question.domain.ports.outbound.NotificacionPort;
import co.edu.unicauca.saberpro.question.domain.ports.outbound.ValidacionJustificacionPort;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/** El mismo recorrido por la API contra cada adaptador. No cambia ni una línea entre uno y otro. */
@AutoConfigureMockMvc
@Import(FlujoRestBaseTest.Dobles.class)
abstract class FlujoRestBaseTest {

    @TestConfiguration
    static class Dobles {

        @Bean
        @Primary
        NotificacionPort notificacionDePrueba() {
            return new NotificacionFake();
        }

        @Bean
        @Primary
        ValidacionJustificacionPort validacionDePrueba() {
            return new ValidacionFake();
        }
    }

    @Autowired
    MockMvc mvc;

    @Autowired
    EvaluarPreguntaUseCase evaluar;

    final ObjectMapper json = new ObjectMapper();

    static String cuerpo(String justificacion, int opciones) {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= opciones; i++) {
            sb.append(i > 1 ? "," : "").append("{\"texto\":\"Opción ").append(i).append("\",\"correcta\":")
                    .append(i == 1).append("}");
        }
        return "{\"enunciado\":\"¿Qué separa el dominio de la infraestructura?\",\"justificacion\":\"" + justificacion
                + "\",\"opciones\":[" + sb + "]}";
    }

    String crear(String cuerpo) throws Exception {
        String respuesta = mvc.perform(post("/api/preguntas").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return json.readTree(respuesta).get("id").asText();
    }

    @Test
    void listarSinPreguntasDevuelve200() throws Exception {
        mvc.perform(get("/api/preguntas")).andExpect(status().isOk());
    }

    @Test
    void creaUnaPreguntaEnBorrador() throws Exception {
        mvc.perform(post("/api/preguntas").contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo("El puerto es el contrato del dominio", 4)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("BORRADOR"))
                .andExpect(jsonPath("$.opciones.length()").value(4));
    }

    @Test
    void justificacionCortaDevuelve400() throws Exception {
        mvc.perform(post("/api/preguntas").contentType(MediaType.APPLICATION_JSON).content(cuerpo("corta", 4)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("La justificación debe tener al menos 15 caracteres"));
    }

    @Test
    void laPreguntaCreadaApareceEnLaListaYSeConsultaPorId() throws Exception {
        String id = crear(cuerpo("El puerto es el contrato del dominio", 4));
        mvc.perform(get("/api/preguntas/" + id)).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(id));
        mvc.perform(get("/api/preguntas")).andExpect(status().isOk()).andExpect(jsonPath("$[?(@.id=='" + id + "')]").exists());
    }

    @Test
    void enviarARevisionDevuelve202ConUnOperationId() throws Exception {
        String id = crear(cuerpo("El puerto es el contrato del dominio", 4));
        mvc.perform(post("/api/preguntas/" + id + "/enviar-revision")).andExpect(status().isAccepted())
                .andExpect(jsonPath("$.operationId").isNotEmpty())
                .andExpect(jsonPath("$.preguntaId").value(id));
    }

    @Test
    void cuandoElCicloDeVidaConfirmaLaPreguntaQuedaPendienteDeRevision() throws Exception {
        String id = crear(cuerpo("El puerto es el contrato del dominio", 4));
        mvc.perform(post("/api/preguntas/" + id + "/enviar-revision")).andExpect(status().isAccepted());
        evaluar.confirmarEstado(id, EstadoPregunta.PENDIENTE_REVISION);
        mvc.perform(get("/api/preguntas/" + id)).andExpect(jsonPath("$.estado").value("PENDIENTE_REVISION"));
        mvc.perform(post("/api/preguntas/" + id + "/enviar-revision")).andExpect(status().isBadRequest());
    }

    @Test
    void conTresOpcionesNoSePuedeEnviar() throws Exception {
        String id = crear(cuerpo("El puerto es el contrato del dominio", 3));
        mvc.perform(post("/api/preguntas/" + id + "/enviar-revision")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("La pregunta debe tener exactamente 4 opciones y tiene 3."));
    }

    @Test
    void unaPreguntaQueNoExisteDevuelve404() throws Exception {
        mvc.perform(get("/api/preguntas/no-existe")).andExpect(status().isNotFound());
        mvc.perform(post("/api/preguntas/no-existe/enviar-revision")).andExpect(status().isNotFound());
    }
}
