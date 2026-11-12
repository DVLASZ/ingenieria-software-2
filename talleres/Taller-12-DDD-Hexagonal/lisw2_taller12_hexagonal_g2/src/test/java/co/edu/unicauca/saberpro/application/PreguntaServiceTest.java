package co.edu.unicauca.saberpro.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.edu.unicauca.saberpro.domain.exception.PreguntaNoEncontradaException;
import co.edu.unicauca.saberpro.domain.exception.ReglaNegocioException;
import co.edu.unicauca.saberpro.domain.model.EstadoPregunta;
import co.edu.unicauca.saberpro.domain.model.Pregunta;
import co.edu.unicauca.saberpro.domain.ports.inbound.CrearPreguntaUseCase.CrearPreguntaComando;
import co.edu.unicauca.saberpro.domain.ports.inbound.CrearPreguntaUseCase.OpcionComando;
import co.edu.unicauca.saberpro.infrastructure.adapter.out.persistence.memory.InMemoryQuestionAdapter;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PreguntaServiceTest {

    final List<String> notificadas = new ArrayList<>();
    PreguntaService servicio;

    @BeforeEach
    void preparar() {
        notificadas.clear();
        servicio = new PreguntaService(new InMemoryQuestionAdapter(), p -> notificadas.add(p.id()));
    }

    static CrearPreguntaComando comando(int opciones) {
        List<OpcionComando> lista = new ArrayList<>();
        for (int i = 0; i < opciones; i++) {
            lista.add(new OpcionComando("Opción " + (i + 1), i == 0));
        }
        return new CrearPreguntaComando("¿Qué es un adaptador?", "Traduce entre el puerto y la tecnología", lista);
    }

    @Test
    void crearGuardaLaPreguntaEnBorrador() {
        Pregunta p = servicio.crear(comando(4));
        assertEquals(EstadoPregunta.BORRADOR, p.estado());
        assertEquals(p.id(), servicio.consultar(p.id()).id());
    }

    @Test
    void crearConJustificacionCortaLanzaReglaNegocio() {
        var malo = new CrearPreguntaComando("¿Qué es un adaptador?", "corta", List.of());
        assertThrows(ReglaNegocioException.class, () -> servicio.crear(malo));
    }

    @Test
    void crearSinListaDeOpcionesCreaUnBorradorIncompleto() {
        var sinLista = new CrearPreguntaComando("¿Qué es un adaptador?", "Traduce entre el puerto y la tecnología", null);
        assertEquals(0, servicio.crear(sinLista).opciones().size());
    }

    @Test
    void enviarARevisionCambiaElEstadoYNotifica() {
        Pregunta p = servicio.crear(comando(4));
        Pregunta enviada = servicio.enviarARevision(p.id());
        assertEquals(EstadoPregunta.PENDIENTE_REVISION, enviada.estado());
        assertEquals(EstadoPregunta.PENDIENTE_REVISION, servicio.consultar(p.id()).estado());
        assertEquals(List.of(p.id()), notificadas);
    }

    @Test
    void enviarConTresOpcionesFallaYNoNotifica() {
        Pregunta p = servicio.crear(comando(3));
        assertThrows(ReglaNegocioException.class, () -> servicio.enviarARevision(p.id()));
        assertEquals(EstadoPregunta.BORRADOR, servicio.consultar(p.id()).estado());
        assertTrue(notificadas.isEmpty());
    }

    @Test
    void consultarUnaPreguntaQueNoExisteFalla() {
        assertThrows(PreguntaNoEncontradaException.class, () -> servicio.consultar("no-existe"));
    }

    @Test
    void enviarUnaPreguntaQueNoExisteFalla() {
        assertThrows(PreguntaNoEncontradaException.class, () -> servicio.enviarARevision("no-existe"));
    }
}
