package co.edu.unicauca.saberpro.domain.model;

import static co.edu.unicauca.saberpro.domain.model.EstadoPregunta.APROBADA;
import static co.edu.unicauca.saberpro.domain.model.EstadoPregunta.ARCHIVADA;
import static co.edu.unicauca.saberpro.domain.model.EstadoPregunta.BORRADOR;
import static co.edu.unicauca.saberpro.domain.model.EstadoPregunta.EN_REVISION;
import static co.edu.unicauca.saberpro.domain.model.EstadoPregunta.PENDIENTE_REVISION;
import static co.edu.unicauca.saberpro.domain.model.EstadoPregunta.PUBLICADA;
import static co.edu.unicauca.saberpro.domain.model.EstadoPregunta.RECHAZADA;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class EstadoPreguntaTest {

    // las transiciones permitidas, sin contar archivar
    private static final Map<EstadoPregunta, Set<EstadoPregunta>> PERMITIDAS = new EnumMap<>(Map.of(
            BORRADOR, EnumSet.of(PENDIENTE_REVISION),
            PENDIENTE_REVISION, EnumSet.of(EN_REVISION),
            EN_REVISION, EnumSet.of(APROBADA, RECHAZADA),
            APROBADA, EnumSet.of(PUBLICADA),
            RECHAZADA, EnumSet.of(BORRADOR),
            PUBLICADA, EnumSet.noneOf(EstadoPregunta.class),
            ARCHIVADA, EnumSet.noneOf(EstadoPregunta.class)));

    @Test
    void haySieteEstados() {
        assertEquals(7, EstadoPregunta.values().length);
    }

    @Test
    void soloSePermitenLasTransicionesDelCicloDeVida() {
        for (EstadoPregunta desde : EstadoPregunta.values()) {
            for (EstadoPregunta hacia : EstadoPregunta.values()) {
                if (hacia == ARCHIVADA) {
                    continue;
                }
                assertEquals(PERMITIDAS.get(desde).contains(hacia), desde.puedePasarA(hacia),
                        desde + " a " + hacia);
            }
        }
    }

    @Test
    void cualquierEstadoSePuedeArchivarMenosArchivada() {
        for (EstadoPregunta desde : EstadoPregunta.values()) {
            assertEquals(desde != ARCHIVADA, desde.puedePasarA(ARCHIVADA), desde.name());
        }
    }

    @Test
    void unaRechazadaVuelveAlBorrador() {
        assertTrue(RECHAZADA.puedePasarA(BORRADOR));
    }

    @Test
    void unaPublicadaYaNoVuelveAtras() {
        assertFalse(PUBLICADA.puedePasarA(BORRADOR));
        assertFalse(PUBLICADA.puedePasarA(APROBADA));
    }

    @Test
    void unDestinoNuloNoEsValido() {
        assertFalse(BORRADOR.puedePasarA(null));
    }
}
