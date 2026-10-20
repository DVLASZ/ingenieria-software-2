package co.edu.unicauca.tracking.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Evento ya contado: un mensaje repetido no suma dos veces. */
@Entity
@Table(name = "eventos_procesados")
public class ProcessedEvent {

    @Id
    private String eventId;

    protected ProcessedEvent() {
    }

    public ProcessedEvent(String eventId) {
        this.eventId = eventId;
    }

    public String getEventId() {
        return eventId;
    }
}
