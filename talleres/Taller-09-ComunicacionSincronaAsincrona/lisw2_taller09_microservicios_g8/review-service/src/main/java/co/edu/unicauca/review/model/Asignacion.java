package co.edu.unicauca.review.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

/** Un revisor asignado a un caso, con su evaluación cuando la hace. */
@Entity
@Table(name = "asignaciones")
public class Asignacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "caso_id")
    private CasoDeRevision caso;

    private Long reviewerId;

    @Enumerated(EnumType.STRING)
    private Decision decision;

    @Column(length = 2000)
    private String observacion;

    private Instant evaluadaEn;

    protected Asignacion() {
    }

    Asignacion(CasoDeRevision caso, Long reviewerId) {
        this.caso = caso;
        this.reviewerId = reviewerId;
    }

    public void evaluar(Decision decision, String observacion) {
        this.decision = decision;
        this.observacion = observacion;
        this.evaluadaEn = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Long getReviewerId() {
        return reviewerId;
    }

    public Decision getDecision() {
        return decision;
    }

    public String getObservacion() {
        return observacion;
    }

    public Instant getEvaluadaEn() {
        return evaluadaEn;
    }
}
