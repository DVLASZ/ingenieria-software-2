package co.edu.unicauca.saberpro.domain.exception;

/** Se lanza cuando una invariante del dominio se incumple. */
public class ReglaNegocioException extends DomainException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
