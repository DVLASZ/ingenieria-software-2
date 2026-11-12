package co.edu.unicauca.saberpro.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import co.edu.unicauca.saberpro.domain.exception.ReglaNegocioException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class ObjetosDeValorTest {

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "   "})
    void enunciadoNuloOVacioSeRechaza(String texto) {
        assertThrows(ReglaNegocioException.class, () -> new Enunciado(texto));
    }

    @Test
    void enunciadoQuitaEspaciosDeLosExtremos() {
        assertEquals("¿Qué es un puerto?", new Enunciado("  ¿Qué es un puerto?  ").texto());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "corta", "catorce chars."})
    void justificacionConMenosDeQuinceCaracteresSeRechaza(String texto) {
        assertThrows(ReglaNegocioException.class, () -> new Justificacion(texto));
    }

    @Test
    void justificacionDeQuinceCaracteresSePermite() {
        assertEquals("quince chars..!", new Justificacion("quince chars..!").texto());
    }

    @Test
    void justificacionNoCuentaLosEspaciosDeLosExtremos() {
        assertThrows(ReglaNegocioException.class, () -> new Justificacion("   corta      "));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "  "})
    void opcionSinTextoSeRechaza(String texto) {
        assertThrows(ReglaNegocioException.class, () -> new OpcionRespuesta(texto, true));
    }

    @Test
    void opcionGuardaSuTextoYSuClave() {
        OpcionRespuesta o = new OpcionRespuesta(" Un contrato ", true);
        assertEquals("Un contrato", o.texto());
        assertEquals(true, o.esCorrecta());
    }

    @Test
    void dosValoresConLosMismosDatosSonIguales() {
        assertEquals(new OpcionRespuesta("A", false), new OpcionRespuesta("A", false));
        assertEquals(new Enunciado("x"), new Enunciado("x"));
    }
}
