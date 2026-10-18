package co.edu.unicauca.lifecycle.model;

/**
 * Estados de una pregunta: exactamente el ciclo de vida del proyecto. {@link #puedePasarA(EstadoPregunta)} concentra las
 * transiciones válidas. Cualquier pregunta puede archivarse, porque nunca se borra físicamente.
 */
public enum EstadoPregunta {
    BORRADOR("Borrador", "DRAFT"),
    PENDIENTE_REVISION("Pendiente de revisión", "PENDING_REVIEW"),
    EN_REVISION("En revisión", "IN_REVIEW"),
    APROBADA("Aprobada", "APPROVED"),
    RECHAZADA("Rechazada", "REJECTED"),
    PUBLICADA("Publicada", "PUBLISHED"),
    ARCHIVADA("Archivada", "ARCHIVED");

    private final String etiqueta;
    private final String equivalente;

    EstadoPregunta(String etiqueta, String equivalente) {
        this.etiqueta = etiqueta;
        this.equivalente = equivalente;
    }

    public String etiqueta() {
        return etiqueta;
    }

    /** Nombre del estado en el vocabulario del enunciado del taller. */
    public String equivalente() {
        return equivalente;
    }

    public boolean puedePasarA(EstadoPregunta destino) {
        if (destino == null) {
            return false;
        }
        if (destino == ARCHIVADA) {
            return true;
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
