package co.edu.unicauca.saberpro.validation.domain;

/** la justificación debe tener más de 15 caracteres (sin contar espacios de los extremos) */
public class JustificationRule {

    public static final int MIN_LENGTH = 15;

    public boolean isValid(String justification) {
        return justification != null && justification.trim().length() > MIN_LENGTH;
    }
}
