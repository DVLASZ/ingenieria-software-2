package co.edu.unicauca.simulation.client;

import co.edu.unicauca.simulation.dto.Dtos.PreguntaRemota;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

/** Llamada REST síncrona a question-service para traer el contenido de una pregunta recién publicada. */
@Component
public class QuestionClient {

    private final RestClient rest;

    public QuestionClient(@Value("${servicios.question-url}") String url, RestClient.Builder builder) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(2000);
        factory.setReadTimeout(5000);
        this.rest = builder.baseUrl(url).requestFactory(factory).build();
    }

    public PreguntaRemota obtener(Long questionId) {
        try {
            return rest.get().uri("/questions/{id}", questionId).retrieve().body(PreguntaRemota.class);
        } catch (ResourceAccessException e) {
            throw new ServicioException(503, "question-service no está disponible");
        } catch (RestClientResponseException e) {
            throw new ServicioException(e.getStatusCode().value(), "question-service respondió " + e.getStatusCode().value());
        }
    }
}
