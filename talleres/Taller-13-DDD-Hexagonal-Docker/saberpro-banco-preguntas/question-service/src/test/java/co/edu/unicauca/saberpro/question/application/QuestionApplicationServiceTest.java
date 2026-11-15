package co.edu.unicauca.saberpro.question.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.edu.unicauca.saberpro.question.Fakes.NotificacionFake;
import co.edu.unicauca.saberpro.question.Fakes.ValidacionFake;
import co.edu.unicauca.saberpro.question.domain.exception.PreguntaNoEncontradaException;
import co.edu.unicauca.saberpro.question.domain.exception.ReglaNegocioException;
import co.edu.unicauca.saberpro.question.domain.model.EstadoPregunta;
import co.edu.unicauca.saberpro.question.domain.model.Pregunta;
import co.edu.unicauca.saberpro.question.domain.ports.inbound.CrearPreguntaUseCase.CrearPreguntaComando;
import co.edu.unicauca.saberpro.question.domain.ports.inbound.CrearPreguntaUseCase.OpcionComando;
import co.edu.unicauca.saberpro.question.domain.ports.inbound.EvaluarPreguntaUseCase.SolicitudAceptada;
import co.edu.unicauca.saberpro.question.infrastructure.memory.InMemoryQuestionAdapter;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class QuestionApplicationServiceTest {

    NotificacionFake notificacion;
    QuestionApplicationService servicio;

    @BeforeEach
    void preparar() {
        notificacion = new NotificacionFake();
        servicio = new QuestionApplicationService(new InMemoryQuestionAdapter(), notificacion, new ValidacionFake());
    }

    static CrearPreguntaComando comando(int opciones, String justificacion) {
        List<OpcionComando> lista = new ArrayList<>();
        for (int i = 0; i < opciones; i++) {
            lista.add(new OpcionComando("Opción " + (i + 1), i == 0));
        }
        return new CrearPreguntaComando("¿Qué es un adaptador?", justificacion, lista);
    }

    static final String BUENA = "Traduce entre el puerto y la tecnología";

    @Test
    void crearGuardaLaPreguntaEnBorrador() {
        Pregunta p = servicio.crear(comando(4, BUENA));
        assertEquals(EstadoPregunta.BORRADOR, p.estado());
        assertEquals(p.id(), servicio.consultar(p.id()).id());
    }

    @Test
    void crearConJustificacionRechazadaPorValidacionFalla() {
        ReglaNegocioException e = assertThrows(ReglaNegocioException.class, () -> servicio.crear(comando(4, "corta")));
        assertEquals("La justificación debe tener al menos 15 caracteres", e.getMessage());
        assertTrue(servicio.listar().isEmpty());
    }

    @Test
    void crearSinListaDeOpcionesCreaUnBorradorIncompleto() {
        var sinLista = new CrearPreguntaComando("¿Qué es un adaptador?", BUENA, null);
        assertEquals(0, servicio.crear(sinLista).opciones().size());
    }

    @Test
    void listarDevuelveLasPreguntasGuardadas() {
        servicio.crear(comando(4, BUENA));
        servicio.crear(comando(3, BUENA));
        assertEquals(2, servicio.listar().size());
    }

    @Test
    void enviarARevisionPublicaElComandoYNoCambiaElEstadoTodavia() {
        Pregunta p = servicio.crear(comando(4, BUENA));
        SolicitudAceptada aceptada = servicio.enviarARevision(p.id());
        assertEquals(p.id(), aceptada.preguntaId());
        assertEquals(List.of(p.id() + ":" + aceptada.operationId()), notificacion.solicitudes);
        assertEquals(EstadoPregunta.BORRADOR, servicio.consultar(p.id()).estado());
    }

    @Test
    void enviarConTresOpcionesFallaYNoPublica() {
        Pregunta p = servicio.crear(comando(3, BUENA));
        assertThrows(ReglaNegocioException.class, () -> servicio.enviarARevision(p.id()));
        assertTrue(notificacion.solicitudes.isEmpty());
    }

    @Test
    void alConfirmarElEstadoLaPreguntaQuedaPendienteDeRevision() {
        Pregunta p = servicio.crear(comando(4, BUENA));
        servicio.confirmarEstado(p.id(), EstadoPregunta.PENDIENTE_REVISION);
        assertEquals(EstadoPregunta.PENDIENTE_REVISION, servicio.consultar(p.id()).estado());
    }

    @Test
    void confirmarDosVecesElMismoEstadoNoFalla() {
        Pregunta p = servicio.crear(comando(4, BUENA));
        servicio.confirmarEstado(p.id(), EstadoPregunta.PENDIENTE_REVISION);
        servicio.confirmarEstado(p.id(), EstadoPregunta.PENDIENTE_REVISION);
        assertEquals(EstadoPregunta.PENDIENTE_REVISION, servicio.consultar(p.id()).estado());
    }

    @Test
    void volverABorradorDesdePendienteRevisionFalla() {
        Pregunta p = servicio.crear(comando(4, BUENA));
        servicio.confirmarEstado(p.id(), EstadoPregunta.PENDIENTE_REVISION);
        assertThrows(ReglaNegocioException.class, () -> servicio.confirmarEstado(p.id(), EstadoPregunta.BORRADOR));
    }

    @Test
    void unaPreguntaPendienteNoSeEnviaDeNuevo() {
        Pregunta p = servicio.crear(comando(4, BUENA));
        servicio.confirmarEstado(p.id(), EstadoPregunta.PENDIENTE_REVISION);
        assertThrows(ReglaNegocioException.class, () -> servicio.enviarARevision(p.id()));
    }

    @Test
    void consultarOEnviarUnaPreguntaQueNoExisteFalla() {
        assertThrows(PreguntaNoEncontradaException.class, () -> servicio.consultar("no-existe"));
        assertThrows(PreguntaNoEncontradaException.class, () -> servicio.enviarARevision("no-existe"));
        assertThrows(PreguntaNoEncontradaException.class,
                () -> servicio.confirmarEstado("no-existe", EstadoPregunta.PENDIENTE_REVISION));
    }
}
