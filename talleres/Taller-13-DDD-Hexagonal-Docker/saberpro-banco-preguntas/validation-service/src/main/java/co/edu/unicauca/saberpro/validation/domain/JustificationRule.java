package co.edu.unicauca.saberpro.validation.domain;

/** la justificación debe tener al menos 15 caracteres, sin contar los espacios de los extremos */
public class JustificationRule {

    public static final int MIN_LENGTH = 15;

    public boolean isValid(String justification) {
        return justification != null && justification.trim().length() >= MIN_LENGTH;
    }
}
