package co.edu.unicauca.saberpro.state;

import co.edu.unicauca.saberpro.builder.QuestionBuilder;
import co.edu.unicauca.saberpro.builder.QuestionDirector;
import co.edu.unicauca.saberpro.domain.model.Competence;
import co.edu.unicauca.saberpro.domain.model.Difficulty;
import co.edu.unicauca.saberpro.domain.model.Question;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Patrón State: ciclo de vida de la pregunta")
class QuestionStateTest {

    private QuestionLifecycle lifecycle;

    @BeforeEach
    void setUp() {
        lifecycle = new QuestionLifecycle(new QuestionDirector(new QuestionBuilder()).buildSampleQuestion());
    }

    private QuestionLifecycle enPendiente() {
        lifecycle.submitForReview();
        return lifecycle;
    }

    private QuestionLifecycle enRevision() {
        enPendiente().startReview();
        return lifecycle;
    }

    private QuestionLifecycle aprobada() {
        enRevision().approve();
        return lifecycle;
    }

    private Question otraVersion() {
        return new QuestionDirector(new QuestionBuilder()).buildMultipleChoice("Contexto nuevo", "¿Versión editada?",
                List.of("a", "b", "c", "d"), 0, "J", Competence.ENGLISH, "T", "S", Difficulty.BASIC);
    }

    private void assertNoPermitida(Runnable accion, String estado) {
        IllegalStateException error = assertThrows(IllegalStateException.class, accion::run);
        assertTrue(error.getMessage().contains("Acción no permitida en estado " + estado), error.getMessage());
    }

    // ---- DraftState ----

    @Test
    @DisplayName("una pregunta nueva empieza en Borrador y no es publicable")
    void empiezaEnBorrador() {
        assertInstanceOf(DraftState.class, lifecycle.getState());
        assertEquals("Borrador", lifecycle.getState().name());
        assertFalse(lifecycle.isPublishable());
    }

    @Test
    @DisplayName("en Borrador se puede editar la pregunta")
    void borradorPermiteEditar() {
        Question editada = otraVersion();

        lifecycle.edit(editada);

        assertSame(editada, lifecycle.getQuestion());
    }

    @Test
    @DisplayName("en Borrador solo se puede enviar a revisión")
    void borradorSoloEnvia() {
        assertNoPermitida(lifecycle::startReview, "Borrador");
        assertNoPermitida(lifecycle::approve, "Borrador");
        assertNoPermitida(lifecycle::requestChanges, "Borrador");

        lifecycle.submitForReview();

        assertInstanceOf(PendingReviewState.class, lifecycle.getState());
    }

    // ---- PendingReviewState ----

    @Test
    @DisplayName("en Pendiente de revisión el autor no puede modificar ni reenviar la pregunta")
    void pendienteBloqueaAlAutor() {
        enPendiente();
        Question original = lifecycle.getQuestion();

        assertNoPermitida(() -> lifecycle.edit(otraVersion()), "Pendiente de revisión");
        assertNoPermitida(lifecycle::submitForReview, "Pendiente de revisión");
        assertSame(original, lifecycle.getQuestion());
    }

    @Test
    @DisplayName("en Pendiente de revisión no se puede aprobar sin haber iniciado la revisión")
    void pendienteNoAprueba() {
        enPendiente();

        assertNoPermitida(lifecycle::approve, "Pendiente de revisión");
        assertNoPermitida(lifecycle::requestChanges, "Pendiente de revisión");
    }

    @Test
    @DisplayName("al iniciar la revisión pasa de Pendiente de revisión a En revisión")
    void pendientePasaARevision() {
        enPendiente().startReview();

        assertInstanceOf(ReviewState.class, lifecycle.getState());
    }

    // ---- ReviewState ----

    @Test
    @DisplayName("en revisión el revisor puede aprobar y la pregunta queda Aprobada")
    void revisionAprueba() {
        enRevision().approve();

        assertInstanceOf(ApprovedState.class, lifecycle.getState());
    }

    @Test
    @DisplayName("en revisión el revisor puede devolver la pregunta y vuelve a Borrador")
    void revisionDevuelve() {
        enRevision().requestChanges();

        assertInstanceOf(DraftState.class, lifecycle.getState());
    }

    @Test
    @DisplayName("en revisión no se puede editar ni reenviar")
    void revisionNoEdita() {
        enRevision();

        assertNoPermitida(() -> lifecycle.edit(otraVersion()), "En revisión");
        assertNoPermitida(lifecycle::submitForReview, "En revisión");
        assertNoPermitida(lifecycle::startReview, "En revisión");
    }

    @Test
    @DisplayName("una pregunta devuelta se puede corregir y volver a enviar")
    void cicloDeCorreccion() {
        enRevision().requestChanges();
        Question corregida = otraVersion();

        lifecycle.edit(corregida);
        lifecycle.submitForReview();

        assertSame(corregida, lifecycle.getQuestion());
        assertInstanceOf(PendingReviewState.class, lifecycle.getState());
    }

    // ---- ApprovedState ----

    @Test
    @DisplayName("una pregunta Aprobada es publicable")
    void aprobadaEsPublicable() {
        aprobada();

        assertEquals("Aprobada", lifecycle.getState().name());
        assertTrue(lifecycle.isPublishable());
    }

    @Test
    @DisplayName("una pregunta Aprobada es un estado final: ninguna operación es válida")
    void aprobadaEsFinal() {
        aprobada();

        assertNoPermitida(() -> lifecycle.edit(otraVersion()), "Aprobada");
        assertNoPermitida(lifecycle::submitForReview, "Aprobada");
        assertNoPermitida(lifecycle::startReview, "Aprobada");
        assertNoPermitida(lifecycle::approve, "Aprobada");
        assertNoPermitida(lifecycle::requestChanges, "Aprobada");
    }

    // ---- Historial ----

    @Test
    @DisplayName("el historial registra los estados por los que pasó la pregunta")
    void historial() {
        enRevision().requestChanges();
        lifecycle.submitForReview();
        lifecycle.startReview();
        lifecycle.approve();

        assertEquals(List.of("Borrador", "Pendiente de revisión", "En revisión", "Borrador",
                "Pendiente de revisión", "En revisión", "Aprobada"), lifecycle.getHistory());
    }

    @Test
    @DisplayName("una operación rechazada no cambia el estado ni el historial")
    void rechazoNoCambiaNada() {
        assertThrows(IllegalStateException.class, lifecycle::approve);

        assertEquals(List.of("Borrador"), lifecycle.getHistory());
        assertInstanceOf(DraftState.class, lifecycle.getState());
    }
}
