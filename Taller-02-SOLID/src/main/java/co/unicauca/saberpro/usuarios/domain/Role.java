package co.unicauca.saberpro.usuarios.domain;

/**
 * Roles definidos en RF-03 del proyecto de curso (HU01. Gestion de usuarios
 * del sistema).
 */
public enum Role {
    ADMINISTRADOR("Administrador"),
    AUTOR_PREGUNTAS("Autor de preguntas"),
    REVISOR("Revisor"),
    DOCENTE("Docente"),
    ESTUDIANTE("Estudiante");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
