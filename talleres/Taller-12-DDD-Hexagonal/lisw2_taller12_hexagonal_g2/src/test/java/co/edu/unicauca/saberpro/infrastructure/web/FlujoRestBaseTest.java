package co.edu.unicauca.saberpro.infrastructure.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/** El mismo recorrido por la API, ejecutado contra cada adaptador. No cambia ni una línea entre uno y otro. */
@AutoConfigureMockMvc
abstract class FlujoRestBaseTest {

    @Autowired
    MockMvc mvc;

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
        JsonNode nodo = json.readTree(respuesta);
        return nodo.get("id").asText();
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
                .andExpect(jsonPath("$.mensaje").value("La justificación debe tener al menos 15 caracteres."));
    }

    @Test
    void consultaLaPreguntaCreada() throws Exception {
        String id = crear(cuerpo("El puerto es el contrato del dominio", 4));
        mvc.perform(get("/api/preguntas/" + id)).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(id));
    }

    @Test
    void enviaARevisionUnaPreguntaCompleta() throws Exception {
        String id = crear(cuerpo("El puerto es el contrato del dominio", 4));
        mvc.perform(post("/api/preguntas/" + id + "/enviar-revision")).andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("PENDIENTE_REVISION"));
        mvc.perform(get("/api/preguntas/" + id)).andExpect(jsonPath("$.estado").value("PENDIENTE_REVISION"));
    }

    @Test
    void conTresOpcionesNoSePuedeEnviar() throws Exception {
        String id = crear(cuerpo("El puerto es el contrato del dominio", 3));
        mvc.perform(post("/api/preguntas/" + id + "/enviar-revision")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("La pregunta debe tener exactamente 4 opciones y tiene 3."));
        mvc.perform(get("/api/preguntas/" + id)).andExpect(jsonPath("$.estado").value("BORRADOR"));
    }

    @Test
    void enviarDosVecesDevuelve400() throws Exception {
        String id = crear(cuerpo("El puerto es el contrato del dominio", 4));
        mvc.perform(post("/api/preguntas/" + id + "/enviar-revision")).andExpect(status().isOk());
        mvc.perform(post("/api/preguntas/" + id + "/enviar-revision")).andExpect(status().isBadRequest());
    }

    @Test
    void unaPreguntaQueNoExisteDevuelve404() throws Exception {
        mvc.perform(get("/api/preguntas/no-existe")).andExpect(status().isNotFound());
        mvc.perform(post("/api/preguntas/no-existe/enviar-revision")).andExpect(status().isNotFound());
    }
}
