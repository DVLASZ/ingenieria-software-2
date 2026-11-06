package co.edu.unicauca.saberpro.lifecycle.application;

/** aviso para question-service: el estado ya cambió */
public record QuestionStatusChanged(Long questionId, String status, String operationId) {
}
