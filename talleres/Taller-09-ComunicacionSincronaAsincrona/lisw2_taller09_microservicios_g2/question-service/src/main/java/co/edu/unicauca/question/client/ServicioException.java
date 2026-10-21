package co.edu.unicauca.question.client;

/** Fallo al llamar a otro servicio por REST: conserva el código HTTP que respondió (o 503 si no respondió). */
public class ServicioException extends RuntimeException {

    private final int status;

    public ServicioException(int status, String message) {
        super(message);
        this.status = status;
    }

    public int status() {
        return status;
    }
}
