package co.edu.unicauca.saberpro.domain.exception;

public class DomainException extends RuntimeException {

    public DomainException(String mensaje) {
        super(mensaje);
    }
}
