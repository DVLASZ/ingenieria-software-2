package co.edu.unicauca.question.model;

/** Estados del proyecto. Aquí es solo una copia para listar y filtrar; la autoridad es lifecycle-service. */
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

    public String equivalente() {
        return equivalente;
    }
}
