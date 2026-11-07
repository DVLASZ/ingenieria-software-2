package co.edu.unicauca.saberpro.domain.model;

import co.edu.unicauca.saberpro.domain.builder.QuestionBuilder;
import co.edu.unicauca.saberpro.domain.exception.InvalidTransitionException;
import co.edu.unicauca.saberpro.domain.model.state.DraftState;
import co.edu.unicauca.saberpro.domain.model.state.IQuestionState;
import co.edu.unicauca.saberpro.domain.model.state.PendingReviewState;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QuestionStateTest {

    private Question draftQuestion() {
        return new QuestionBuilder().statement("enunciado")
                .addOption("a", true).addOption("b", false).addOption("c", false).addOption("d", false)
                .build();
    }

    @Test
    void borradorPasaAPendienteDeRevision() {
        IQuestionState siguiente = new DraftState().submitForReview();

        assertThat(siguiente).isInstanceOf(PendingReviewState.class);
        assertThat(siguiente.status()).isEqualTo(QuestionStatus.PENDING_REVIEW);
    }

    @Test
    void pendienteNoSePuedeEnviarOtraVez() {
        assertThatThrownBy(() -> new PendingReviewState().submitForReview())
                .isInstanceOf(InvalidTransitionException.class)
                .hasMessageContaining("pendiente de revisión");
    }

    @Test
    void laPreguntaCambiaDeEstadoAlEnviarse() {
        Question q = draftQuestion();

        q.submitForReview();

        assertThat(q.getStatus()).isEqualTo(QuestionStatus.PENDING_REVIEW);
    }

    @Test
    void laPreguntaPendienteRechazaUnSegundoEnvio() {
        Question q = draftQuestion();
        q.submitForReview();

        assertThatThrownBy(q::submitForReview).isInstanceOf(InvalidTransitionException.class);
        assertThat(q.getStatus()).isEqualTo(QuestionStatus.PENDING_REVIEW);
    }

    @Test
    void ensureCanSubmitNoCambiaElEstado() {
        Question q = draftQuestion();

        q.ensureCanSubmit();

        assertThat(q.getStatus()).isEqualTo(QuestionStatus.DRAFT);
    }

    @Test
    void ensureCanSubmitFallaSiYaEstaPendiente() {
        Question q = draftQuestion().withStatus(QuestionStatus.PENDING_REVIEW);

        assertThatThrownBy(q::ensureCanSubmit).isInstanceOf(InvalidTransitionException.class);
    }

    @Test
    void elEnumCreaElEstadoQueLeCorresponde() {
        assertThat(QuestionStatus.DRAFT.newState()).isInstanceOf(DraftState.class);
        assertThat(QuestionStatus.PENDING_REVIEW.newState()).isInstanceOf(PendingReviewState.class);
    }

    @Test
    void withStatusDevuelveUnaCopiaConOtroEstado() {
        Question original = draftQuestion();

        Question copia = original.withStatus(QuestionStatus.PENDING_REVIEW);

        assertThat(copia.getStatus()).isEqualTo(QuestionStatus.PENDING_REVIEW);
        assertThat(original.getStatus()).isEqualTo(QuestionStatus.DRAFT);
        assertThat(copia.getOptions()).isEqualTo(original.getOptions());
    }

    /** el enunciado prohíbe if/else/switch en la transición: se revisa el código fuente de los estados */
    @Test
    void losEstadosNoUsanCondicionales() throws IOException {
        Pattern condicional = Pattern.compile("\\b(if|else|switch|case)\\b|\\?[^:]*:");
        Path base = Path.of("src/main/java/co/edu/unicauca/saberpro/domain/model/state");

        for (String archivo : List.of("DraftState.java", "PendingReviewState.java", "IQuestionState.java")) {
            String codigo = Files.readString(base.resolve(archivo));
            String sinComentarios = codigo.replaceAll("(?s)/\\*.*?\\*/", "").replaceAll("//.*", "");
            assertThat(condicional.matcher(sinComentarios).find())
                    .as("%s no debe tener condicionales", archivo)
                    .isFalse();
        }
    }
}
