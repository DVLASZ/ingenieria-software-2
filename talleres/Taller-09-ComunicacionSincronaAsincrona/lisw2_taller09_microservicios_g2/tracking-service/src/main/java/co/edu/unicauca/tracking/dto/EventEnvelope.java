package co.edu.unicauca.tracking.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/** Sobre común de todos los mensajes: identificador, tipo, instante y cuerpo con identificadores. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record EventEnvelope(String eventId, String type, String occurredAt, Map<String, Object> data) {

    public static EventEnvelope of(String type, Map<String, Object> data) {
        return new EventEnvelope(UUID.randomUUID().toString(), type, Instant.now().toString(), data);
    }

    public Long longOf(String key) {
        Object v = data == null ? null : data.get(key);
        return v instanceof Number n ? Long.valueOf(n.longValue()) : null;
    }

    public String stringOf(String key) {
        Object v = data == null ? null : data.get(key);
        return v == null ? null : String.valueOf(v);
    }
}
