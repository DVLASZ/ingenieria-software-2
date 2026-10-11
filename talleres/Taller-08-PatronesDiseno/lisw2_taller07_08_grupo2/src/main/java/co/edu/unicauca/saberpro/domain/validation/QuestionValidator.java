package co.edu.unicauca.saberpro.domain.validation;

import co.edu.unicauca.saberpro.domain.model.Option;
import co.edu.unicauca.saberpro.domain.model.Question;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Validación estructural del banco (adaptada de las reglas del proyecto de curso). El Builder ya garantiza que la
 * pregunta esté completa; aquí se revisan reglas de calidad del contenido.
 */
public class QuestionValidator {

    /** Devuelve los incumplimientos de la pregunta; una lista vacía significa que es válida. */
    public List<String> validate(Question question) {
        List<String> violations = new ArrayList<>();

        long marks = question.getStatement().chars().filter(c -> c == '?').count();
        if (marks != 1) {
            violations.add("el enunciado debe formular una única pregunta (tiene " + marks + " signos de interrogación)");
        }

        Set<String> texts = new HashSet<>();
        for (Option option : question.getOptions()) {
            if (!texts.add(option.text().toLowerCase())) {
                violations.add("la opción " + option.label() + " repite el texto de otra opción");
            }
        }
        return violations;
    }

    public boolean isValid(Question question) {
        return validate(question).isEmpty();
    }
}
