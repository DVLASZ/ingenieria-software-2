package co.edu.unicauca.history.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/** Un hecho del historial: qué pasó, quién lo hizo y cuándo. Nunca se modifica ni se borra. */
@Entity
@Table(name = "entradas")
public class Entrada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Evento del que viene; evita registrar dos veces el mismo hecho. */
    @Column(unique = true, nullable = false)
    private String eventId;

    private String tipo;

    /** Nulo en los eventos de seguridad (inicios de sesión). */
    private Long questionId;

    private String actor;
    private String resumen;

    @Column(length = 4000)
    private String detalles;

    private Instant ocurridaEn;
    private Instant registradaEn = Instant.now();

    protected Entrada() {
    }

    public Entrada(String eventId, String tipo, Long questionId, String actor, String resumen, String detalles, Instant ocurridaEn) {
        this.eventId = eventId;
        this.tipo = tipo;
        this.questionId = questionId;
        this.actor = actor;
        this.resumen = resumen;
        this.detalles = detalles;
        this.ocurridaEn = ocurridaEn;
    }

    public Long getId() {
        return id;
    }

    public String getEventId() {
        return eventId;
    }

    public String getTipo() {
        return tipo;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public String getActor() {
        return actor;
    }

    public String getResumen() {
        return resumen;
    }

    public String getDetalles() {
        return detalles;
    }

    public Instant getOcurridaEn() {
        return ocurridaEn;
    }

    public Instant getRegistradaEn() {
        return registradaEn;
    }
}
