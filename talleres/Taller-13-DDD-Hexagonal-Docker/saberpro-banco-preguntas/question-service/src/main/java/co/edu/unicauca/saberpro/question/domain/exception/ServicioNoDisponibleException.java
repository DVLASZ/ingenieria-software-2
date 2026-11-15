package co.edu.unicauca.saberpro.question.domain.exception;

/** Algo de afuera que el dominio necesita (validación o mensajería) no respondió. */
public class ServicioNoDisponibleException extends DomainException {

    public ServicioNoDisponibleException(String mensaje) {
        super(mensaje);
    }
}
