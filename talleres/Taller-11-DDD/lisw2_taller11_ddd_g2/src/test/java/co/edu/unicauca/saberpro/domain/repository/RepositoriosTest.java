package co.edu.unicauca.saberpro.domain.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.edu.unicauca.saberpro.domain.model.Dictamen;
import co.edu.unicauca.saberpro.domain.model.EstadoPregunta;
import co.edu.unicauca.saberpro.domain.model.Pregunta;
import co.edu.unicauca.saberpro.domain.model.Revision;
import co.edu.unicauca.saberpro.domain.model.Revisor;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Muestra que los contratos alcanzan para el flujo de revisión sin tocar ninguna tecnología de persistencia. */
class RepositoriosTest {

    private PreguntaRepository preguntas;
    private RevisionRepository revisiones;

    @BeforeEach
    void preparar() {
        preguntas = new RepositoriosEnMemoria.Preguntas();
        revisiones = new RepositoriosEnMemoria.Revisiones();
    }

    private static Pregunta lista(String autor) {
        Pregunta p = new Pregunta(autor);
        p.redactarEnunciado("¿Cuál es la idea principal del texto?");
        p.justificar("La idea principal resume el argumento central.");
        p.agregarOpcion("Una tesis", true);
        p.agregarOpcion("Una anécdota", false);
        p.agregarOpcion("Una lista de datos", false);
        p.agregarOpcion("Una instrucción", false);
        return p;
    }

    @Test
    void alGuardarUnaPreguntaNuevaRecibeIdentidad() {
        Pregunta guardada = preguntas.guardar(lista("autor-1"));

        assertEquals(1L, guardada.id());
        assertSame(guardada, preguntas.buscarPorId(1L).orElseThrow());
        assertTrue(preguntas.buscarPorId(99L).isEmpty());
    }

    @Test
    void sePuedenConsultarPorAutorYPorEstado() {
        Pregunta a = preguntas.guardar(lista("autor-1"));
        preguntas.guardar(lista("autor-2"));
        a.enviarARevision();
        preguntas.guardar(a);

        assertEquals(1, preguntas.buscarPorAutor("autor-1").size());
        assertEquals(List.of(a), preguntas.buscarPorEstado(EstadoPregunta.PENDIENTE_REVISION));
        assertEquals(1, preguntas.buscarPorEstado(EstadoPregunta.BORRADOR).size());
    }

    @Test
    void elFlujoDeRevisionSeCoordinaConLosDosContratos() {
        Pregunta p = preguntas.guardar(lista("autor-1"));
        p.enviarARevision();

        // asignar revisores: la pregunta pasa a En revisión y se abre la revisión
        Revision r = revisiones.guardar(Revision.abrir(p.id(), p.autorId(),
                List.of(new Revisor("d-1", "Ana"), new Revisor("d-2", "Beatriz"))));
        p.iniciarRevision();
        preguntas.guardar(p);

        // los dos revisores aprueban: la pregunta queda aprobada
        r.evaluar("d-1", Dictamen.APROBADO, "");
        r.evaluar("d-2", Dictamen.APROBADO, "");
        if (r.dictamen().orElseThrow() == Dictamen.APROBADO) {
            p.aprobar();
        }
        preguntas.guardar(p);

        assertEquals(EstadoPregunta.APROBADA, preguntas.buscarPorId(p.id()).orElseThrow().estado());
        assertEquals(Dictamen.APROBADO, revisiones.buscarPorPregunta(p.id()).orElseThrow().dictamen().orElseThrow());
        assertSame(r, revisiones.buscarPorId(r.id()).orElseThrow());
        assertTrue(revisiones.buscarPorPregunta(99L).isEmpty());
    }
}
