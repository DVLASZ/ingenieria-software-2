package co.edu.unicauca.saberpro.domain.exception;

public class PreguntaNoEncontradaException extends DomainException {

    public PreguntaNoEncontradaException(String id) {
        super("No existe una pregunta con id " + id + ".");
    }
}
