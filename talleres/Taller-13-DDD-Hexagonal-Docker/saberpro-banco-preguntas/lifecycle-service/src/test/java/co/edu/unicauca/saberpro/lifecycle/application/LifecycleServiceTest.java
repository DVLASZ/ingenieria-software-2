package co.edu.unicauca.saberpro.lifecycle.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class LifecycleServiceTest {

    final List<EstadoCambiado> publicados = new ArrayList<>();
    final LifecycleService service = new LifecycleService(publicados::add);

    @Test
    void deBorradorPasaAPendienteRevisionYPublicaElEvento() {
        EstadoCambiado evento = service.enviarARevision(new SolicitudRevision("op-1", "p-1", "BORRADOR"));
        assertEquals("PENDIENTE_REVISION", evento.estadoNuevo());
        assertEquals("BORRADOR", evento.estadoAnterior());
        assertEquals("op-1", evento.operationId());
        assertEquals(List.of(evento), publicados);
    }

    @Test
    void desdePendienteRevisionNoSePuedeEnviarDeNuevo() {
        assertThrows(TransicionInvalidaException.class,
                () -> service.enviarARevision(new SolicitudRevision("op-2", "p-1", "PENDIENTE_REVISION")));
        assertTrue(publicados.isEmpty());
    }

    @Test
    void unEstadoDesconocidoSeRechaza() {
        assertThrows(IllegalArgumentException.class,
                () -> service.enviarARevision(new SolicitudRevision("op-3", "p-1", "PUBLICADA")));
    }

    @Test
    void sinEstadoActualSeRechaza() {
        assertThrows(IllegalArgumentException.class,
                () -> service.enviarARevision(new SolicitudRevision("op-4", "p-1", null)));
    }
}
