package co.edu.unicauca.saberpro.question.infrastructure.jpa;

import co.edu.unicauca.saberpro.domain.builder.QuestionBuilder;
import co.edu.unicauca.saberpro.domain.model.Question;
import co.edu.unicauca.saberpro.domain.model.QuestionStatus;
import co.edu.unicauca.saberpro.question.TestQuestions;
import co.edu.unicauca.saberpro.question.infrastructure.jpa.mapper.QuestionDomainMapper;
import co.edu.unicauca.saberpro.question.infrastructure.jpa.record.QuestionRecord;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class QuestionDomainMapperTest {

    private final QuestionDomainMapper mapper = new QuestionDomainMapper();

    @Test
    void convierteDeDominioARecordConSusOpciones() {
        QuestionRecord record = mapper.toRecord(TestQuestions.draft());

        assertThat(record.getId()).isNull();
        assertThat(record.getStatus()).isEqualTo(QuestionStatus.DRAFT);
        assertThat(record.getOptions()).hasSize(4);
        assertThat(record.getOptions()).allSatisfy(o -> assertThat(o.getQuestion()).isSameAs(record));
        assertThat(record.getOptions().get(0).isCorrect()).isTrue();
    }

    @Test
    void idaYVueltaDejaLaPreguntaIgual() {
        Question original = new QuestionBuilder()
                .id(4L)
                .statement("enunciado")
                .justification("una justificación larga")
                .status(QuestionStatus.PENDING_REVIEW)
                .addOption("a", false).addOption("b", true).addOption("c", false).addOption("d", false)
                .build();

        Question vuelta = mapper.toDomain(mapper.toRecord(original));

        assertThat(vuelta.getId()).isEqualTo(4L);
        assertThat(vuelta.getStatement()).isEqualTo(original.getStatement());
        assertThat(vuelta.getJustification()).isEqualTo(original.getJustification());
        assertThat(vuelta.getOptions()).isEqualTo(original.getOptions());
        assertThat(vuelta.getStatus()).isEqualTo(QuestionStatus.PENDING_REVIEW);
    }
}
