package co.edu.unicauca.saberpro.question.application.event;

/** aviso de lifecycle-service cuando ya cambió el estado */
public record QuestionStatusChanged(Long questionId, String status, String operationId) {
}
