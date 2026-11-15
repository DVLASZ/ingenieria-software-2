package co.edu.unicauca.saberpro.question.domain.exception;

/** Se lanza cuando una invariante o una regla del negocio se incumple. */
public class ReglaNegocioException extends DomainException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
