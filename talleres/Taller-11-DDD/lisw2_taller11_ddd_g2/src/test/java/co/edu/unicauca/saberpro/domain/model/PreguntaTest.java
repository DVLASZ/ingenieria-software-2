package co.edu.unicauca.saberpro.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.edu.unicauca.saberpro.domain.exception.ReglaNegocioException;
import org.junit.jupiter.api.Test;

class PreguntaTest {

    private static Pregunta completa() {
        Pregunta p = new Pregunta("docente-1");
        p.redactarEnunciado("¿Cuál es la idea principal del texto?");
        p.justificar("La idea principal resume el argumento central del autor.");
        p.agregarOpcion("El autor defiende una tesis", true);
        p.agregarOpcion("El autor cuenta una historia", false);
        p.agregarOpcion("El autor lista datos", false);
        p.agregarOpcion("El autor da instrucciones", false);
        return p;
    }

    // la prueba que propone la guía

    @Test
    void noDebePermitirEnviarARevisionSinCuatroOpciones() {
        Pregunta pregunta = new Pregunta();
        pregunta.agregarOpcion("Opción A", true);
        assertThrows(ReglaNegocioException.class, pregunta::enviarARevision);
    }

    // invariantes

    @Test
    void unaPreguntaNuevaEsUnBorradorEnLaVersionUno() {
        Pregunta p = new Pregunta("docente-1");

        assertEquals(EstadoPregunta.BORRADOR, p.estado());
        assertEquals(1, p.version());
        assertTrue(p.opciones().isEmpty());
    }

    @Test
    void sePuedeEnviarUnaPreguntaCompleta() {
        Pregunta p = completa();

        p.enviarARevision();

        assertEquals(EstadoPregunta.PENDIENTE_REVISION, p.estado());
    }

    @Test
    void noSePuedeEnviarConMasDeUnaClave() {
        Pregunta p = new Pregunta("docente-1");
        p.redactarEnunciado("¿Qué dice el texto?");
        p.justificar("Se justifica con el segundo párrafo del texto.");
        p.agregarOpcion("A", true);
        p.agregarOpcion("B", true);
        p.agregarOpcion("C", false);
        p.agregarOpcion("D", false);

        assertThrows(ReglaNegocioException.class, p::enviarARevision);
        assertEquals(EstadoPregunta.BORRADOR, p.estado());
    }

    @Test
    void noSePuedeEnviarSinClave() {
        Pregunta p = new Pregunta("docente-1");
        p.redactarEnunciado("¿Qué dice el texto?");
        p.justificar("Se justifica con el segundo párrafo del texto.");
        p.agregarOpcion("A", false);
        p.agregarOpcion("B", false);
        p.agregarOpcion("C", false);
        p.agregarOpcion("D", false);

        assertThrows(ReglaNegocioException.class, p::enviarARevision);
    }

    @Test
    void noSePuedeEnviarSinEnunciado() {
        Pregunta p = new Pregunta("docente-1");
        p.justificar("Se justifica con el segundo párrafo del texto.");
        p.agregarOpcion("A", true);
        p.agregarOpcion("B", false);
        p.agregarOpcion("C", false);
        p.agregarOpcion("D", false);

        assertThrows(ReglaNegocioException.class, p::enviarARevision);
    }

    @Test
    void noSePuedeEnviarSinJustificacion() {
        Pregunta p = new Pregunta("docente-1");
        p.redactarEnunciado("¿Qué dice el texto?");
        p.agregarOpcion("A", true);
        p.agregarOpcion("B", false);
        p.agregarOpcion("C", false);
        p.agregarOpcion("D", false);

        assertThrows(ReglaNegocioException.class, p::enviarARevision);
    }

    @Test
    void noSePuedeEnviarSinAutor() {
        Pregunta p = new Pregunta();
        p.redactarEnunciado("¿Qué dice el texto?");
        p.justificar("Se justifica con el segundo párrafo del texto.");
        p.agregarOpcion("A", true);
        p.agregarOpcion("B", false);
        p.agregarOpcion("C", false);
        p.agregarOpcion("D", false);

        assertThrows(ReglaNegocioException.class, p::enviarARevision);
    }

    @Test
    void noSeAgreganMasDeCuatroOpciones() {
        Pregunta p = completa();

        assertThrows(ReglaNegocioException.class, () -> p.agregarOpcion("El autor pregunta", false));
        assertEquals(4, p.opciones().size());
    }

    @Test
    void noSeAgregaUnaOpcionRepetida() {
        Pregunta p = new Pregunta("docente-1");
        p.agregarOpcion("El autor defiende una tesis", true);

        assertThrows(ReglaNegocioException.class, () -> p.agregarOpcion("el autor defiende una TESIS", false));
    }

    @Test
    void unaOpcionInvalidaNoQuedaAgregada() {
        Pregunta p = new Pregunta("docente-1");

        assertThrows(ReglaNegocioException.class, () -> p.agregarOpcion("Todas las anteriores", false));
        assertTrue(p.opciones().isEmpty());
    }

    @Test
    void sePuedeQuitarUnaOpcionYReemplazarla() {
        Pregunta p = completa();

        p.quitarOpcion(3);
        p.agregarOpcion("El autor hace una pregunta", false);

        assertEquals(4, p.opciones().size());
        assertThrows(ReglaNegocioException.class, () -> p.quitarOpcion(9));
    }

    @Test
    void laListaDeOpcionesNoSePuedeModificarPorFuera() {
        Pregunta p = completa();

        assertThrows(UnsupportedOperationException.class, () -> p.opciones().clear());
        assertThrows(UnsupportedOperationException.class, () -> p.opciones().add(new OpcionRespuesta("X", false)));
    }

    @Test
    void laClaveEsLaOpcionCorrecta() {
        assertEquals("El autor defiende una tesis", completa().clave().orElseThrow().texto());
        assertTrue(new Pregunta().clave().isEmpty());
    }

    // solo se edita en borrador

    @Test
    void despuesDeEnviarNoSePuedeModificarNada() {
        Pregunta p = completa();
        p.enviarARevision();

        assertThrows(ReglaNegocioException.class, () -> p.agregarOpcion("Otra", false));
        assertThrows(ReglaNegocioException.class, () -> p.quitarOpcion(0));
        assertThrows(ReglaNegocioException.class, () -> p.redactarEnunciado("Otro enunciado"));
        assertThrows(ReglaNegocioException.class, () -> p.justificar("Otra justificación distinta"));
    }

    @Test
    void elEnunciadoYLaJustificacionInvalidosNoReemplazanALosActuales() {
        Pregunta p = completa();

        assertThrows(ReglaNegocioException.class, () -> p.redactarEnunciado(" "));
        assertThrows(ReglaNegocioException.class, () -> p.justificar("corta"));

        assertEquals("¿Cuál es la idea principal del texto?", p.enunciado().orElseThrow().texto());
        assertTrue(p.justificacion().orElseThrow().texto().startsWith("La idea principal"));
    }

    // ciclo de vida

    @Test
    void recorreElCicloHastaPublicarse() {
        Pregunta p = completa();

        p.enviarARevision();
        p.iniciarRevision();
        p.aprobar();
        p.publicar();

        assertEquals(EstadoPregunta.PUBLICADA, p.estado());
    }

    @Test
    void noSePuedeSaltarUnEstado() {
        Pregunta p = completa();

        assertThrows(ReglaNegocioException.class, p::iniciarRevision);
        assertThrows(ReglaNegocioException.class, p::aprobar);
        assertThrows(ReglaNegocioException.class, p::publicar);
        p.enviarARevision();
        assertThrows(ReglaNegocioException.class, p::aprobar);
        assertThrows(ReglaNegocioException.class, p::enviarARevision);
    }

    @Test
    void unaRechazadaSeReabreComoVersionNueva() {
        Pregunta p = completa();
        p.enviarARevision();
        p.iniciarRevision();
        p.rechazar();

        p.reabrir();
        p.quitarOpcion(3);
        p.agregarOpcion("El autor hace una pregunta", false);
        p.enviarARevision();

        assertEquals(2, p.version());
        assertEquals(EstadoPregunta.PENDIENTE_REVISION, p.estado());
    }

    @Test
    void soloSeReabreUnaRechazada() {
        Pregunta p = completa();

        assertThrows(ReglaNegocioException.class, p::reabrir);
        assertEquals(1, p.version());
    }

    @Test
    void cualquierPreguntaSeArchivaUnaSolaVez() {
        Pregunta p = completa();

        p.archivar();

        assertEquals(EstadoPregunta.ARCHIVADA, p.estado());
        assertThrows(ReglaNegocioException.class, p::archivar);
        assertFalse(p.estado().puedePasarA(EstadoPregunta.BORRADOR));
    }

    // identidad

    @Test
    void laIdentidadSeAsignaUnaSolaVez() {
        Pregunta p = new Pregunta("docente-1");

        p.asignarIdentidad(7L);

        assertEquals(7L, p.id());
        assertThrows(ReglaNegocioException.class, () -> p.asignarIdentidad(8L));
    }

    @Test
    void dosPreguntasConLaMismaIdentidadSonLaMismaEntidad() {
        Pregunta a = new Pregunta("docente-1");
        Pregunta b = new Pregunta("docente-2");
        a.asignarIdentidad(1L);
        b.asignarIdentidad(1L);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(new Pregunta(), new Pregunta());
    }
}
