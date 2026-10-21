package co.edu.unicauca.question.model;

/** Comandos que question-service publica para que lifecycle-service cambie el estado de una pregunta. */
public enum Comando {
    ENVIAR_A_REVISION("SubmitQuestionForReview", "question.submit"),
    PUBLICAR("PublishQuestion", "question.publish"),
    ARCHIVAR("ArchiveQuestion", "question.archive");

    private final String tipo;
    private final String routingKey;

    Comando(String tipo, String routingKey) {
        this.tipo = tipo;
        this.routingKey = routingKey;
    }

    public String tipo() {
        return tipo;
    }

    public String routingKey() {
        return routingKey;
    }
}
