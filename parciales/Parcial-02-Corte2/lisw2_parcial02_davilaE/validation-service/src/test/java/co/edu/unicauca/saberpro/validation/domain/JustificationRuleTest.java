package co.edu.unicauca.saberpro.validation.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JustificationRuleTest {

    private final JustificationRule rule = new JustificationRule();

    @Test
    void de16CaracteresEsValida() {
        assertThat(rule.isValid("1234567890123456")).isTrue();
    }

    @Test
    void de15CaracteresNoAlcanza() {
        assertThat(rule.isValid("123456789012345")).isFalse();
    }

    @Test
    void losEspaciosDeLosExtremosNoCuentan() {
        assertThat(rule.isValid("   corta   ")).isFalse();
    }

    @Test
    void nulaOVaciaFalla() {
        assertThat(rule.isValid(null)).isFalse();
        assertThat(rule.isValid("")).isFalse();
    }
}
