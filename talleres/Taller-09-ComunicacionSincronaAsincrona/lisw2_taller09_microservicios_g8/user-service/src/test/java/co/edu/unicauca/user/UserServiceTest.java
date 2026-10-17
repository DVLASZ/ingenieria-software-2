package co.edu.unicauca.user;

import co.edu.unicauca.user.model.Rol;
import co.edu.unicauca.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.rabbitmq.listener.simple.auto-startup=false")
@AutoConfigureMockMvc
class UserServiceTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    UserService servicio;

    @MockitoBean
    RabbitTemplate rabbitTemplate;

    @Test
    void loginValidoDevuelveUsuarioRolesYTokenYPublicaElEvento() throws Exception {
        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"autor1\",\"password\":\"clave123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authenticated").value(true))
                .andExpect(jsonPath("$.user.roles[0]").value("AUTOR"))
                .andExpect(jsonPath("$.token").isNotEmpty());
        verify(rabbitTemplate).convertAndSend(eq("saberpro.topic.exchange"), eq("auth.login-succeeded"), any(Object.class));
    }

    @Test
    void loginIncorrectoResponde401YPublicaElEventoFallido() throws Exception {
        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"autor1\",\"password\":\"mala\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.authenticated").value(false));
        verify(rabbitTemplate).convertAndSend(eq("saberpro.topic.exchange"), eq("auth.login-failed"), any(Object.class));
    }

    @Test
    void loginSinDatosEs400() throws Exception {
        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content("{\"username\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void losRevisoresElegiblesNoIncluyenAlExcluido() throws Exception {
        assertThat(servicio.porRol(Rol.REVISOR, null)).hasSize(4);
        Long primero = servicio.porRol(Rol.REVISOR, null).get(0).getId();
        mvc.perform(get("/users").param("role", "REVISOR").param("excluir", primero.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    void unUsuarioInexistenteEs404() throws Exception {
        mvc.perform(get("/users/9999")).andExpect(status().isNotFound());
    }
}
