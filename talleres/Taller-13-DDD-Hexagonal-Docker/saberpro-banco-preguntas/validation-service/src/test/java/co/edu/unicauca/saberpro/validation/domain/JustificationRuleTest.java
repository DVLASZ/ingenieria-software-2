package co.edu.unicauca.saberpro.validation.domain;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class JustificationRuleTest {

    final JustificationRule regla = new JustificationRule();

    @Test
    void conQuinceCaracteresEsValida() {
        assertTrue(regla.isValid("quince chars..!"));
    }

    @Test
    void conCatorceCaracteresNoEsValida() {
        assertFalse(regla.isValid("catorce chars."));
    }

    @Test
    void nulaOVaciaNoEsValida() {
        assertFalse(regla.isValid(null));
        assertFalse(regla.isValid("      "));
    }

    @Test
    void losEspaciosDeLosExtremosNoCuentan() {
        assertFalse(regla.isValid("   corta      "));
    }
}
