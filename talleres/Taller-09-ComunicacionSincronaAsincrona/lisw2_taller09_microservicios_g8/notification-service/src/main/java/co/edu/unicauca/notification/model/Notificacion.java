package co.edu.unicauca.notification.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;

/** Una notificación para un usuario. El par (evento, destinatario) es único: un evento repetido no avisa dos veces. */
@Entity
@Table(name = "notificaciones", uniqueConstraints = @UniqueConstraint(columnNames = {"eventId", "destinatarioId"}))
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String eventId;
    private Long destinatarioId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType tipo;

    private Long questionId;
    private String asunto;

    @Column(length = 2000)
    private String mensaje;

    /** `ENVIADA` cuando el envío simulado termina bien. */
    private String estado = "ENVIADA";

    private Instant creadaEn = Instant.now();

    protected Notificacion() {
    }

    public Notificacion(String eventId, Long destinatarioId, NotificationType tipo, Long questionId, String asunto, String mensaje) {
        this.eventId = eventId;
        this.destinatarioId = destinatarioId;
        this.tipo = tipo;
        this.questionId = questionId;
        this.asunto = asunto;
        this.mensaje = mensaje;
    }

    public Long getId() {
        return id;
    }

    public String getEventId() {
        return eventId;
    }

    public Long getDestinatarioId() {
        return destinatarioId;
    }

    public NotificationType getTipo() {
        return tipo;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public String getAsunto() {
        return asunto;
    }

    public String getMensaje() {
        return mensaje;
    }

    public String getEstado() {
        return estado;
    }

    public Instant getCreadaEn() {
        return creadaEn;
    }
}
