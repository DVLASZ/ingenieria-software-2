package co.edu.unicauca.saberpro.lifecycle.application;

/** Estados que este servicio conoce y la única transición que hace: de BORRADOR a PENDIENTE_REVISION. */
public enum EstadoCiclo {
    BORRADOR,
    PENDIENTE_REVISION;

    public static EstadoCiclo desde(String texto) {
        if (texto == null) {
            throw new IllegalArgumentException("El estado actual es obligatorio");
        }
        return EstadoCiclo.valueOf(texto.trim().toUpperCase());
    }

    public EstadoCiclo alEnviarARevision() {
        if (this != BORRADOR) {
            throw new TransicionInvalidaException("No se puede enviar a revisión desde " + this);
        }
        return PENDIENTE_REVISION;
    }
}
