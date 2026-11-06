package co.edu.unicauca.saberpro.lifecycle.application;

/** el comando que manda question-service (mismo JSON, clase propia de este servicio) */
public record SubmitQuestionForReview(String operationId, Long questionId, String currentStatus) {
}
