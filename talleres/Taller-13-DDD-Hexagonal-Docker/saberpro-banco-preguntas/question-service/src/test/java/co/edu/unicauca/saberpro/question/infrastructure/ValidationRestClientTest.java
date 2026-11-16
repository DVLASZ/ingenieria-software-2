package co.edu.unicauca.saberpro.question.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import co.edu.unicauca.saberpro.question.domain.exception.ServicioNoDisponibleException;
import co.edu.unicauca.saberpro.question.domain.ports.outbound.ValidacionJustificacionPort.Resultado;
import co.edu.unicauca.saberpro.question.infrastructure.rest.ValidationRestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

class ValidationRestClientTest {

    MockRestServiceServer servidor;
    ValidationRestClient cliente;

    @BeforeEach
    void preparar() {
        RestTemplate plantilla = new RestTemplate();
        servidor = MockRestServiceServer.bindTo(plantilla).build();
        cliente = new ValidationRestClient(plantilla, "http://validation-service:8082");
    }

    @Test
    void llamaAValidateYDevuelveElResultado() {
        servidor.expect(requestTo("http://validation-service:8082/validate")).andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath("$.justification").value("Una justificación larga"))
                .andRespond(withSuccess("{\"valid\":true,\"message\":\"Justificación válida\"}", MediaType.APPLICATION_JSON));
        Resultado r = cliente.validar("Una justificación larga");
        assertTrue(r.valida());
        assertEquals("Justificación válida", r.mensaje());
        servidor.verify();
    }

    @Test
    void siElServicioDiceQueNoEsValidaLoReporta() {
        servidor.expect(requestTo("http://validation-service:8082/validate"))
                .andRespond(withSuccess("{\"valid\":false,\"message\":\"corta\"}", MediaType.APPLICATION_JSON));
        assertFalse(cliente.validar("x").valida());
    }

    @Test
    void siElServicioFallaLanzaServicioNoDisponible() {
        servidor.expect(requestTo("http://validation-service:8082/validate")).andRespond(withServerError());
        assertThrows(ServicioNoDisponibleException.class, () -> cliente.validar("Una justificación larga"));
    }
}
