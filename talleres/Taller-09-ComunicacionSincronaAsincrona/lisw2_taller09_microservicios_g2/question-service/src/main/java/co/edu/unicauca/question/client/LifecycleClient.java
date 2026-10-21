package co.edu.unicauca.question.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.HashMap;
import java.util.Map;

/** Llamadas REST síncronas a lifecycle-service. */
@Component
public class LifecycleClient {

    private static final ObjectMapper JSON = new ObjectMapper();

    private final RestClient rest;

    public LifecycleClient(@Value("${servicios.lifecycle-url}") String url, RestClient.Builder builder) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(2000);
        factory.setReadTimeout(5000);
        this.rest = builder.baseUrl(url).requestFactory(factory).build();
    }

    /** La pregunta nace en Borrador en lifecycle. */
    public void crearEstado(Long questionId, Long autorId, String titulo, String competencia) {
        Map<String, Object> cuerpo = new HashMap<>();
        cuerpo.put("questionId", questionId);
        cuerpo.put("autorId", autorId);
        cuerpo.put("titulo", titulo);
        cuerpo.put("competencia", competencia);
        ejecutar(() -> rest.post().uri("/lifecycle").body(cuerpo).retrieve().toBodilessEntity());
    }

    public void transicionar(Long questionId, String hasta, String solicitadoPor, String motivo) {
        Map<String, Object> cuerpo = new HashMap<>();
        cuerpo.put("to", hasta);
        cuerpo.put("requestedBy", solicitadoPor);
        cuerpo.put("motivo", motivo);
        ejecutar(() -> rest.post().uri("/lifecycle/{id}/transition", questionId).body(cuerpo).retrieve().toBodilessEntity());
    }

    private void ejecutar(Runnable llamada) {
        try {
            llamada.run();
        } catch (ResourceAccessException e) {
            throw new ServicioException(503, "lifecycle-service no está disponible");
        } catch (RestClientResponseException e) {
            throw new ServicioException(e.getStatusCode().value(), mensaje(e));
        }
    }

    private static String mensaje(RestClientResponseException e) {
        try {
            JsonNode m = JSON.readTree(e.getResponseBodyAsString()).path("message");
            return m.isMissingNode() ? "lifecycle-service respondió " + e.getStatusCode().value() : m.asText();
        } catch (Exception ignorada) {
            return "lifecycle-service respondió " + e.getStatusCode().value();
        }
    }
}
