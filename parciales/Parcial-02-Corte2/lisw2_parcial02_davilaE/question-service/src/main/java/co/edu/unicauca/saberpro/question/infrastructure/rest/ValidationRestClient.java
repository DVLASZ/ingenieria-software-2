package co.edu.unicauca.saberpro.question.infrastructure.rest;

import co.edu.unicauca.saberpro.question.application.exception.ValidationServiceUnavailableException;
import co.edu.unicauca.saberpro.question.application.port.IValidationClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/** llama por REST (síncrono) a POST /validate de validation-service */
@Component
public class ValidationRestClient implements IValidationClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public ValidationRestClient(RestTemplate restTemplate, @Value("${validation.service.url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    @Override
    public ValidationResult validate(String justification) {
        try {
            ValidationBody body = restTemplate.postForObject(baseUrl + "/validate",
                    new ValidationPayload(justification), ValidationBody.class);
            if (body == null) {
                throw new ValidationServiceUnavailableException("validation-service respondió sin contenido", null);
            }
            return new ValidationResult(body.valid(), body.message());
        } catch (RestClientException e) {
            throw new ValidationServiceUnavailableException("No se pudo consultar validation-service en " + baseUrl, e);
        }
    }

    record ValidationPayload(String justification) {
    }

    record ValidationBody(boolean valid, String message) {
    }
}
