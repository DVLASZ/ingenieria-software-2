package co.edu.unicauca.saberpro.question.infrastructure.rest;

import co.edu.unicauca.saberpro.question.domain.exception.ServicioNoDisponibleException;
import co.edu.unicauca.saberpro.question.domain.ports.outbound.ValidacionJustificacionPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/** Adaptador de salida: llama por REST, de forma síncrona, a POST /validate de validation-service. */
@Component
public class ValidationRestClient implements ValidacionJustificacionPort {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public ValidationRestClient(RestTemplate restTemplate, @Value("${validation.service.url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    @Override
    public Resultado validar(String justificacion) {
        try {
            Respuesta cuerpo = restTemplate.postForObject(baseUrl + "/validate", new Peticion(justificacion), Respuesta.class);
            if (cuerpo == null) {
                throw new ServicioNoDisponibleException("validation-service respondió sin contenido");
            }
            return new Resultado(cuerpo.valid(), cuerpo.message());
        } catch (RestClientException e) {
            throw new ServicioNoDisponibleException("No se pudo consultar validation-service en " + baseUrl);
        }
    }

    record Peticion(String justification) {
    }

    record Respuesta(boolean valid, String message) {
    }
}
