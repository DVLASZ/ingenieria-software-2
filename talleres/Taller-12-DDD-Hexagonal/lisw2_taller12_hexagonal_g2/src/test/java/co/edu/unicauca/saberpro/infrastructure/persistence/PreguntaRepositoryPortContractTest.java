package co.edu.unicauca.saberpro.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.edu.unicauca.saberpro.domain.model.Enunciado;
import co.edu.unicauca.saberpro.domain.model.EstadoPregunta;
import co.edu.unicauca.saberpro.domain.model.Justificacion;
import co.edu.unicauca.saberpro.domain.model.OpcionRespuesta;
import co.edu.unicauca.saberpro.domain.model.Pregunta;
import co.edu.unicauca.saberpro.domain.ports.outbound.PreguntaRepositoryPort;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Las mismas pruebas para cualquier adaptador del puerto. Cada adaptador las hereda. */
abstract class PreguntaRepositoryPortContractTest {

    abstract PreguntaRepositoryPort puerto();

    static Pregunta nueva() {
        return Pregunta.crear(new Enunciado("¿Qué es un puerto?"), new Justificacion("Es el contrato que define el dominio"),
                List.of(new OpcionRespuesta("Un contrato", true), new OpcionRespuesta("Una tabla", false),
                        new OpcionRespuesta("Un botón", false), new OpcionRespuesta("Un archivo", false)));
    }

    @Test
    void guardaYEncuentraPorId() {
        Pregunta p = puerto().save(nueva());
        Pregunta leida = puerto().findById(p.id()).orElseThrow();
        assertEquals(p.id(), leida.id());
        assertEquals("¿Qué es un puerto?", leida.enunciado().texto());
        assertEquals("Es el contrato que define el dominio", leida.justificacion().texto());
        assertEquals(EstadoPregunta.BORRADOR, leida.estado());
    }

    @Test
    void conservaLasOpcionesYElOrden() {
        Pregunta p = puerto().save(nueva());
        List<OpcionRespuesta> opciones = puerto().findById(p.id()).orElseThrow().opciones();
        assertEquals(p.opciones(), opciones);
        assertTrue(opciones.get(0).esCorrecta());
    }

    @Test
    void unIdInexistenteDevuelveVacio() {
        assertTrue(puerto().findById("no-existe").isEmpty());
    }

    @Test
    void guardarDeNuevoActualizaElEstado() {
        Pregunta p = puerto().save(nueva());
        p.enviarARevision();
        puerto().save(p);
        assertEquals(EstadoPregunta.PENDIENTE_REVISION, puerto().findById(p.id()).orElseThrow().estado());
    }

    @Test
    void cambiarLoLeidoNoCambiaLoGuardado() {
        Pregunta p = puerto().save(nueva());
        Pregunta leida = puerto().findById(p.id()).orElseThrow();
        leida.enviarARevision();
        assertEquals(EstadoPregunta.BORRADOR, puerto().findById(p.id()).orElseThrow().estado());
        assertNotSame(leida, puerto().findById(p.id()).orElseThrow());
    }
}
