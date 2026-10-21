package co.edu.unicauca.lifecycle.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/** Resultado del procesamiento de un comando asíncrono, consultable por el cliente. */
@Entity
@Table(name = "operation_result")
public class OperationResult {

    @Id
    private String operationId;

    private Long questionId;
    private String tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OperationStatus status;

    @Column(length = 2000)
    private String mensaje;

    private Instant completedAt;

    protected OperationResult() {
    }

    public OperationResult(String operationId, Long questionId, String tipo, OperationStatus status, String mensaje) {
        this.operationId = operationId;
        this.questionId = questionId;
        this.tipo = tipo;
        this.status = status;
        this.mensaje = mensaje;
        this.completedAt = Instant.now();
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

    public Instant getCompletedAt() {
        return completedAt;
    }
}
