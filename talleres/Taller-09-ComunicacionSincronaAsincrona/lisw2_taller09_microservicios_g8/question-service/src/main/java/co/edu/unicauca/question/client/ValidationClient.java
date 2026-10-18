package co.edu.unicauca.question.client;

import co.edu.unicauca.question.dto.Dtos.PreguntaRequest;
import co.edu.unicauca.question.dto.Dtos.ResultadoValidacion;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.Map;

/** Llamada REST síncrona a validation-service. */
@Component
public class ValidationClient {

    private final RestClient rest;

    public ValidationClient(@Value("${servicios.validation-url}") String url, RestClient.Builder builder) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(2000);
        factory.setReadTimeout(5000);
        this.rest = builder.baseUrl(url).requestFactory(factory).build();
    }

    public ResultadoValidacion validar(PreguntaRequest pregunta) {
        Map<String, Object> cuerpo = new java.util.HashMap<>();
        cuerpo.put("autorId", pregunta.autorId());
        cuerpo.put("titulo", pregunta.titulo());
        cuerpo.put("contexto", pregunta.contexto());
        cuerpo.put("pregunta", pregunta.pregunta());
        cuerpo.put("opciones", pregunta.opciones());
        cuerpo.put("respuestaCorrecta", pregunta.respuestaCorrecta());
        try {
            return rest.post().uri("/validate").body(cuerpo).retrieve().body(ResultadoValidacion.class);
        } catch (ResourceAccessException e) {
            throw new ServicioException(503, "validation-service no está disponible");
        } catch (RestClientResponseException e) {
            throw new ServicioException(502, "validation-service respondió " + e.getStatusCode().value());
        }
    }
}
