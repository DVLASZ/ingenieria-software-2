package co.edu.unicauca.lifecycle.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/** Una fila del historial de estados de una pregunta. */
@Entity
@Table(name = "state_transition")
public class StateTransition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long questionId;

    @Enumerated(EnumType.STRING)
    private EstadoPregunta desde;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPregunta hasta;

    private String requestedBy;

    @Column(length = 2000)
    private String motivo;

    private Instant ocurridaEn;

    protected StateTransition() {
    }

    public StateTransition(Long questionId, EstadoPregunta desde, EstadoPregunta hasta, String requestedBy, String motivo) {
        this.questionId = questionId;
        this.desde = desde;
        this.hasta = hasta;
        this.requestedBy = requestedBy;
        this.motivo = motivo;
        this.ocurridaEn = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public EstadoPregunta getDesde() {
        return desde;
    }

    public EstadoPregunta getHasta() {
        return hasta;
    }

    public String getRequestedBy() {
        return requestedBy;
    }

    public String getMotivo() {
        return motivo;
    }

    public Instant getOcurridaEn() {
        return ocurridaEn;
    }
}
