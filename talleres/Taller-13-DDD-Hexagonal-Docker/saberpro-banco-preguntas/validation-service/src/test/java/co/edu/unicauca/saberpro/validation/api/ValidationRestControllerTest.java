package co.edu.unicauca.saberpro.validation.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ValidationRestController.class)
class ValidationRestControllerTest {

    @Autowired
    MockMvc mvc;

    @Test
    void justificacionLargaEsValida() throws Exception {
        mvc.perform(post("/validate").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"justification\":\"El microkernel deja el núcleo mínimo\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.valid").value(true));
    }

    @Test
    void justificacionCortaRespondeOkConValidFalse() throws Exception {
        mvc.perform(post("/validate").contentType(MediaType.APPLICATION_JSON).content("{\"justification\":\"corta\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.valid").value(false))
                .andExpect(jsonPath("$.message").value("La justificación debe tener al menos 15 caracteres"));
    }
}
