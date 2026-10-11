package co.edu.unicauca.saberpro.facade;

/**
 * Único tipo de error que lanza la fachada: así quien la usa no necesita conocer las excepciones internas del
 * Builder ni del validador.
 */
public class QuestionBankException extends RuntimeException {

    public QuestionBankException(String message, Throwable cause) {
        super(message, cause);
    }
}
