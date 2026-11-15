package co.edu.unicauca.saberpro.question.domain.exception;

public class PreguntaNoEncontradaException extends DomainException {

    public PreguntaNoEncontradaException(String id) {
        super("No existe una pregunta con id " + id + ".");
    }
}
