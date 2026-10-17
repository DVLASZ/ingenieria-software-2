package co.edu.unicauca.validation;

import co.edu.unicauca.validation.dto.Dtos.ValidationRequest;
import co.edu.unicauca.validation.service.QuestionValidator;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.rabbitmq.listener.simple.auto-startup=false")
@AutoConfigureMockMvc
class ValidationServiceTest {

    private static final String VALIDA = """
            {"autorId":2,"titulo":"T","contexto":"Un texto de contexto","pregunta":"¿Cuál es la idea principal?",
             "opciones":["Una","Otra","Una tercera","Una cuarta"],"respuestaCorrecta":"B"}""";

    @Autowired
    MockMvc mvc;

    @Autowired
    QuestionValidator validador;

    @MockitoBean
    RabbitTemplate rabbitTemplate;

    private ValidationRequest pregunta(String contexto, String enunciado, List<String> opciones, String clave) {
        return new ValidationRequest(2L, "T", contexto, enunciado, opciones, clave);
    }

    @Test
    void unaPreguntaCorrectaNoTieneErrores() {
        assertThat(validador.validar(pregunta("ctx", "¿Qué es?", List.of("a", "b", "c", "d"), "A"))).isEmpty();
    }

    @Test
    void exigeContextoYUnaUnicaPreguntaDirecta() {
        assertThat(validador.validar(pregunta(" ", "¿Qué es? ¿Y esto?", List.of("a", "b", "c", "d"), "A")))
                .anyMatch(e -> e.contains("contexto")).anyMatch(e -> e.contains("única pregunta"));
    }

    @Test
    void exigeExactamenteCuatroOpciones() {
        assertThat(validador.validar(pregunta("ctx", "¿Qué es?", List.of("a", "b", "c"), "A")))
                .contains("Deben ser exactamente cuatro opciones");
    }

    @Test
    void rechazaTodasYNingunaDeLasAnterioresYOpcionesRepetidas() {
        assertThat(validador.validar(pregunta("ctx", "¿Qué es?", List.of("a", "Todas las anteriores", "NINGUNA DE LAS ANTERIORES", "a"), "A")))
                .anyMatch(e -> e.contains("opción B")).anyMatch(e -> e.contains("opción C")).anyMatch(e -> e.contains("repetida"));
    }

    @Test
    void laRespuestaCorrectaDebeSerUnaLetraValida() {
        assertThat(validador.validar(pregunta("ctx", "¿Qué es?", List.of("a", "b", "c", "d"), "E")))
                .anyMatch(e -> e.contains("respuesta correcta"));
    }

    @Test
    void validarUnaPreguntaCorrectaResponde200ValidaYNoPublica() throws Exception {
        mvc.perform(post("/validate").contentType(MediaType.APPLICATION_JSON).content(VALIDA))
                .andExpect(status().isOk()).andExpect(jsonPath("$.valid").value(true));
        verify(rabbitTemplate, never()).convertAndSend(any(String.class), any(String.class), any(Object.class));
    }

    @Test
    void validarUnaPreguntaInvalidaInformaLosErroresYPublicaValidationFailed() throws Exception {
        mvc.perform(post("/validate").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"autorId\":2,\"titulo\":\"T\",\"contexto\":\"\",\"pregunta\":\"sin signo\",\"opciones\":[\"a\"],\"respuestaCorrecta\":\"A\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(false))
                .andExpect(jsonPath("$.errors.length()").value(3));
        verify(rabbitTemplate).convertAndSend(eq("saberpro.topic.exchange"), eq("validation.failed"), any(Object.class));
    }
}
