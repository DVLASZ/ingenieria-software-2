package co.edu.unicauca.question.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/** Operación asíncrona pedida por el cliente; su resultado llega después desde lifecycle-service. */
@Entity
@Table(name = "operaciones")
public class Operation {

    @Id
    private String operationId;

    private Long questionId;
    private String tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OperationStatus status = OperationStatus.PENDING;

    @Column(length = 2000)
    private String mensaje;

    private Instant creadaEn = Instant.now();
    private Instant completadaEn;

    protected Operation() {
    }

    public Operation(String operationId, Long questionId, String tipo) {
        this.operationId = operationId;
        this.questionId = questionId;
        this.tipo = tipo;
    }

    public void registrarResultado(OperationStatus status, String mensaje) {
        this.status = status;
        this.mensaje = mensaje;
        this.completadaEn = Instant.now();
    }

    public String getOperationId() {
        return operationId;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public String getTipo() {
        return tipo;
    }

    public OperationStatus getStatus() {
        return status;
    }

    public String getMensaje() {
        return mensaje;
    }

    public Instant getCreadaEn() {
        return creadaEn;
    }

    public Instant getCompletadaEn() {
        return completadaEn;
    }
}
