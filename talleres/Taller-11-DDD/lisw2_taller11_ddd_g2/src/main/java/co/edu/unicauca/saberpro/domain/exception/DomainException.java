package co.edu.unicauca.saberpro.domain.exception;

/** Error de negocio: algo que el dominio no permite. No depende de ningún framework. */
public class DomainException extends RuntimeException {

    public DomainException(String mensaje) {
        super(mensaje);
    }
}
