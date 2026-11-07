package co.edu.unicauca.saberpro.domain.builder;

import co.edu.unicauca.saberpro.domain.exception.InvalidQuestionException;
import co.edu.unicauca.saberpro.domain.model.Question;
import co.edu.unicauca.saberpro.domain.model.QuestionStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QuestionBuilderTest {

    private IQuestionBuilder validBuilder() {
        return new QuestionBuilder()
                .statement("¿Qué patrón separa el núcleo de los módulos?")
                .justification("El microkernel deja el núcleo mínimo y carga plugins")
                .addOption("Microkernel", true)
                .addOption("Monolito", false)
                .addOption("Singleton", false)
                .addOption("Facade", false);
    }

    @Test
    void armaUnaPreguntaValidaEnBorrador() {
        Question q = validBuilder().build();

        assertThat(q.getStatement()).startsWith("¿Qué patrón");
        assertThat(q.getOptions()).hasSize(4);
        assertThat(q.getOptions().stream().filter(o -> o.correct()).count()).isEqualTo(1);
        assertThat(q.getStatus()).isEqualTo(QuestionStatus.DRAFT);
        assertThat(q.getId()).isNull();
    }

    @Test
    void permiteRestaurarIdYEstado() {
        Question q = validBuilder().id(7L).status(QuestionStatus.PENDING_REVIEW).build();

        assertThat(q.getId()).isEqualTo(7L);
        assertThat(q.getStatus()).isEqualTo(QuestionStatus.PENDING_REVIEW);
    }

    @Test
    void rechazaTresOpciones() {
        IQuestionBuilder b = new QuestionBuilder()
                .statement("enunciado")
                .addOption("a", true).addOption("b", false).addOption("c", false);

        assertThatThrownBy(b::build)
                .isInstanceOf(InvalidQuestionException.class)
                .hasMessageContaining("exactamente 4 opciones");
    }

    @Test
    void rechazaCincoOpciones() {
        IQuestionBuilder b = validBuilder().addOption("e", false);

        assertThatThrownBy(b::build).isInstanceOf(InvalidQuestionException.class);
    }

    @Test
    void rechazaSinOpcionCorrecta() {
        IQuestionBuilder b = new QuestionBuilder().statement("enunciado")
                .addOption("a", false).addOption("b", false).addOption("c", false).addOption("d", false);

        assertThatThrownBy(b::build)
                .isInstanceOf(InvalidQuestionException.class)
                .hasMessageContaining("1 opción correcta");
    }

    @Test
    void rechazaDosOpcionesCorrectas() {
        IQuestionBuilder b = new QuestionBuilder().statement("enunciado")
                .addOption("a", true).addOption("b", true).addOption("c", false).addOption("d", false);

        assertThatThrownBy(b::build).isInstanceOf(InvalidQuestionException.class);
    }

    @Test
    void rechazaEnunciadoVacioYOpcionVacia() {
        IQuestionBuilder b = new QuestionBuilder().statement("  ")
                .addOption("a", true).addOption(" ", false).addOption("c", false).addOption("d", false);

        assertThatThrownBy(b::build)
                .isInstanceOfSatisfying(InvalidQuestionException.class,
                        e -> assertThat(e.getErrors()).hasSize(2));
    }

    @Test
    void juntaTodosLosErroresEnUnaSolaExcepcion() {
        assertThatThrownBy(() -> new QuestionBuilder().build())
                .isInstanceOfSatisfying(InvalidQuestionException.class,
                        e -> assertThat(e.getErrors()).hasSize(3));
    }
}
