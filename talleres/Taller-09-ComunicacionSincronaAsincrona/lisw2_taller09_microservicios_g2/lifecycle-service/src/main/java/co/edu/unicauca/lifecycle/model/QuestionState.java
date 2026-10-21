package co.edu.unicauca.lifecycle.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/** Estado actual de una pregunta: lifecycle es el único servicio que lo modifica. */
@Entity
@Table(name = "question_state")
public class QuestionState {

    @Id
    private Long questionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPregunta estado;

    private Long autorId;
    private String titulo;
    private String competencia;
    private String changedBy;
    private Instant changedAt;

    protected QuestionState() {
    }

    public QuestionState(Long questionId, Long autorId, String titulo, String competencia) {
        this.questionId = questionId;
        this.autorId = autorId;
        this.titulo = titulo;
        this.competencia = competencia;
        this.estado = EstadoPregunta.BORRADOR;
        this.changedBy = String.valueOf(autorId);
        this.changedAt = Instant.now();
    }

    public void cambiarA(EstadoPregunta nuevo, String por) {
        this.estado = nuevo;
        this.changedBy = por;
        this.changedAt = Instant.now();
    }

    public Long getQuestionId() {
        return questionId;
    }

    public EstadoPregunta getEstado() {
        return estado;
    }

    public Long getAutorId() {
        return autorId;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getCompetencia() {
        return competencia;
    }

    public String getChangedBy() {
        return changedBy;
    }

    public Instant getChangedAt() {
        return changedAt;
    }
}
