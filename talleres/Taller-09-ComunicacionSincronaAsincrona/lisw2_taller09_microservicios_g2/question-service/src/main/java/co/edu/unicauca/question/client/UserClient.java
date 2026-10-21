package co.edu.unicauca.question.client;

import co.edu.unicauca.question.dto.Dtos.UsuarioInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.Optional;

/** Llamada REST síncrona a user-service para comprobar que el autor existe y tiene el rol AUTOR. */
@Component
public class UserClient {

    private final RestClient rest;

    public UserClient(@Value("${servicios.user-url}") String url, RestClient.Builder builder) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(2000);
        factory.setReadTimeout(5000);
        this.rest = builder.baseUrl(url).requestFactory(factory).build();
    }

    public Optional<UsuarioInfo> buscar(Long id) {
        try {
            return Optional.ofNullable(rest.get().uri("/users/{id}", id).retrieve().body(UsuarioInfo.class));
        } catch (ResourceAccessException e) {
            throw new ServicioException(503, "user-service no está disponible");
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 404) {
                return Optional.empty();
            }
            throw new ServicioException(502, "user-service respondió " + e.getStatusCode().value());
        }
    }
}
