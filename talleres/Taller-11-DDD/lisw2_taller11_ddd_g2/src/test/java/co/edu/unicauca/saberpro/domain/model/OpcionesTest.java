package co.edu.unicauca.saberpro.domain.model;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.edu.unicauca.saberpro.domain.exception.ReglaNegocioException;
import java.util.List;
import org.junit.jupiter.api.Test;

class OpcionesTest {

    private static OpcionRespuesta opcion(String texto, boolean correcta) {
        return new OpcionRespuesta(texto, correcta);
    }

    private static List<OpcionRespuesta> cuatro(int claves) {
        return List.of(opcion("Opción A", claves > 0), opcion("Opción B", claves > 1),
                opcion("Opción C", claves > 2), opcion("Opción D", claves > 3));
    }

    @Test
    void unaOpcionNoPuedeEstarVacia() {
        assertThrows(ReglaNegocioException.class, () -> opcion(null, true));
        assertThrows(ReglaNegocioException.class, () -> opcion("  ", false));
    }

    @Test
    void unaOpcionNoPuedeDecirTodasLasAnteriores() {
        assertThrows(ReglaNegocioException.class, () -> opcion("Todas las anteriores", false));
        assertThrows(ReglaNegocioException.class, () -> opcion("Ninguna de las anteriores", true));
    }

    @Test
    void laOpcionGuardaSuTextoYSiEsLaClave() {
        OpcionRespuesta o = opcion("  Una tesis  ", true);

        assertEquals("Una tesis", o.texto());
        assertTrue(o.esCorrecta());
    }

    @Test
    void dosOpcionesIgualesSonElMismoValor() {
        assertEquals(opcion("A", true), opcion("A", true));
    }

    @Test
    void elTextoRepetidoSeDetectaSinMirarMayusculasNiTildes() {
        assertTrue(opcion("Época", false).mismoTextoQue(opcion("  epoca ", true)));
        assertFalse(opcion("Época", false).mismoTextoQue(opcion("Edad", false)));
    }

    @Test
    void noSeAgreganMasDeCuatroOpciones() {
        assertThrows(ReglaNegocioException.class,
                () -> ValidezOpciones.verificarNuevaOpcion(cuatro(1), opcion("Opción E", false)));
    }

    @Test
    void noSeAgregaUnaOpcionRepetida() {
        assertThrows(ReglaNegocioException.class,
                () -> ValidezOpciones.verificarNuevaOpcion(List.of(opcion("Opción A", true)), opcion("opcion a", false)));
    }

    @Test
    void laOpcionNuevaDistintaSePermite() {
        assertDoesNotThrow(() -> ValidezOpciones.verificarNuevaOpcion(List.of(opcion("Opción A", true)),
                opcion("Opción B", false)));
    }

    @Test
    void paraEnviarSeNecesitanExactamenteCuatroOpciones() {
        assertThrows(ReglaNegocioException.class, () -> ValidezOpciones.verificarParaEnvio(List.of()));
        assertThrows(ReglaNegocioException.class,
                () -> ValidezOpciones.verificarParaEnvio(cuatro(1).subList(0, 3)));
    }

    @Test
    void paraEnviarSeNecesitaExactamenteUnaClave() {
        assertThrows(ReglaNegocioException.class, () -> ValidezOpciones.verificarParaEnvio(cuatro(0)));
        assertThrows(ReglaNegocioException.class, () -> ValidezOpciones.verificarParaEnvio(cuatro(2)));
        assertDoesNotThrow(() -> ValidezOpciones.verificarParaEnvio(cuatro(1)));
    }
}
