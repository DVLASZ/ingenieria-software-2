package co.edu.unicauca.saberpro.question.domain.ports.outbound;

/** Puerto de salida: consultar al servicio de validación si una justificación sirve. */
public interface ValidacionJustificacionPort {

    Resultado validar(String justificacion);

    record Resultado(boolean valida, String mensaje) {
    }
}
