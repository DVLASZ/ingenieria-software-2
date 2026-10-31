package co.edu.unicauca.saberpro.domain.model;

/** Estados del ciclo de vida de una pregunta y las transiciones que el negocio permite entre ellos. */
public enum EstadoPregunta {
    BORRADOR,
    PENDIENTE_REVISION,
    EN_REVISION,
    APROBADA,
    RECHAZADA,
    PUBLICADA,
    ARCHIVADA;

    /** Cualquier pregunta se puede archivar menos la que ya lo está: nunca se borra. */
    public boolean puedePasarA(EstadoPregunta destino) {
        if (destino == null) {
            return false;
        }
        if (destino == ARCHIVADA) {
            return this != ARCHIVADA;
        }
        return switch (this) {
            case BORRADOR -> destino == PENDIENTE_REVISION;
            case PENDIENTE_REVISION -> destino == EN_REVISION;
            case EN_REVISION -> destino == APROBADA || destino == RECHAZADA;
            case APROBADA -> destino == PUBLICADA;
            case RECHAZADA -> destino == BORRADOR;
            case PUBLICADA, ARCHIVADA -> false;
        };
    }
}
