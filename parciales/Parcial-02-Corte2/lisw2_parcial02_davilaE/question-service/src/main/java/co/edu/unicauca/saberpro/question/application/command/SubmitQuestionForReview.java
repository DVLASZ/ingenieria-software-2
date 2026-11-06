package co.edu.unicauca.saberpro.question.application.command;

/** comando que viaja por RabbitMQ hacia lifecycle-service */
public record SubmitQuestionForReview(String operationId, Long questionId, String currentStatus) {
}
