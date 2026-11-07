package co.edu.unicauca.saberpro.question.infrastructure.rest;

import co.edu.unicauca.saberpro.question.application.exception.ValidationServiceUnavailableException;
import co.edu.unicauca.saberpro.question.application.port.IValidationClient.ValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class ValidationRestClientTest {

    private MockRestServiceServer server;
    private ValidationRestClient client;

    @BeforeEach
    void setUp() {
        RestTemplate restTemplate = new RestTemplate();
        server = MockRestServiceServer.createServer(restTemplate);
        client = new ValidationRestClient(restTemplate, "http://localhost:8082");
    }

    @Test
    void mandaLaJustificacionAPostValidateYLeeLaRespuestaValida() {
        server.expect(requestTo("http://localhost:8082/validate"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath("$.justification").value("una justificación larga"))
                .andRespond(withSuccess("{\"valid\":true,\"message\":\"ok\"}", MediaType.APPLICATION_JSON));

        ValidationResult result = client.validate("una justificación larga");

        assertThat(result.valid()).isTrue();
        server.verify();
    }

    @Test
    void traeElMensajeCuandoNoEsValida() {
        server.expect(requestTo("http://localhost:8082/validate"))
                .andRespond(withSuccess("{\"valid\":false,\"message\":\"muy corta\"}", MediaType.APPLICATION_JSON));

        ValidationResult result = client.validate("corta");

        assertThat(result.valid()).isFalse();
        assertThat(result.message()).isEqualTo("muy corta");
    }

    @Test
    void siValidationDevuelve500LanzaNoDisponible() {
        server.expect(requestTo("http://localhost:8082/validate")).andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThatThrownBy(() -> client.validate("j")).isInstanceOf(ValidationServiceUnavailableException.class);
    }

    @Test
    void siNoHayConexionLanzaNoDisponible() {
        // sin MockRestServiceServer: un puerto donde no escucha nadie
        ValidationRestClient sinServidor = new ValidationRestClient(new RestTemplate(), "http://localhost:1");

        assertThatThrownBy(() -> sinServidor.validate("j")).isInstanceOf(ValidationServiceUnavailableException.class);
    }
}
