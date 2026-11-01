package co.edu.unicauca.saberpro.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.edu.unicauca.saberpro.domain.exception.ReglaNegocioException;
import org.junit.jupiter.api.Test;

class ObjetosDeValorTextoTest {

    @Test
    void elEnunciadoNoPuedeSerNulo() {
        assertThrows(ReglaNegocioException.class, () -> new Enunciado(null));
    }

    @Test
    void elEnunciadoNoPuedeEstarVacio() {
        assertThrows(ReglaNegocioException.class, () -> new Enunciado("   "));
    }

    @Test
    void elEnunciadoQuitaLosEspaciosDeLosExtremos() {
        assertEquals("¿Cuál es la idea principal?", new Enunciado("  ¿Cuál es la idea principal?  ").texto());
    }

    @Test
    void dosEnunciadosConElMismoTextoSonIguales() {
        assertEquals(new Enunciado("¿Qué dice el texto?"), new Enunciado("¿Qué dice el texto?"));
        assertNotEquals(new Enunciado("¿Qué dice el texto?"), new Enunciado("¿Qué piensa el autor?"));
    }

    @Test
    void laJustificacionNecesitaQuinceCaracteres() {
        assertThrows(ReglaNegocioException.class, () -> new Justificacion("muy corta"));
        assertThrows(ReglaNegocioException.class, () -> new Justificacion("12345678901234"));
        assertEquals("123456789012345", new Justificacion("123456789012345").texto());
    }

    @Test
    void losEspaciosDeLosExtremosNoCuentanEnLaJustificacion() {
        assertThrows(ReglaNegocioException.class, () -> new Justificacion("   corta       "));
    }

    @Test
    void laJustificacionNoPuedeSerNula() {
        assertThrows(ReglaNegocioException.class, () -> new Justificacion(null));
    }

    @Test
    void laJustificacionNoPuedeUsarExpresionesProhibidas() {
        assertThrows(ReglaNegocioException.class,
                () -> new Justificacion("Es correcta porque son TODAS LAS ANTERIORES juntas"));
        assertThrows(ReglaNegocioException.class,
                () -> new Justificacion("La respuesta es ninguna de las anteriores, sin duda"));
    }

    @Test
    void elFiltroIgnoraMayusculasTildesYEspaciosRepetidos() {
        assertTrue(FiltroExpresion.contieneExpresionProhibida("Todas   las ANTERIORES"));
        assertTrue(FiltroExpresion.contieneExpresionProhibida("Ninguna de las ANTERIORES"));
        assertFalse(FiltroExpresion.contieneExpresionProhibida("Las anteriores no cuentan solas"));
        assertFalse(FiltroExpresion.contieneExpresionProhibida(null));
    }
}
