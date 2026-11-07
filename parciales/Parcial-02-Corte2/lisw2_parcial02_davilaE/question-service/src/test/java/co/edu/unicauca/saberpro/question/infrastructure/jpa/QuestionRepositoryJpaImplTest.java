package co.edu.unicauca.saberpro.question.infrastructure.jpa;

import co.edu.unicauca.saberpro.domain.builder.QuestionBuilder;
import co.edu.unicauca.saberpro.domain.model.AnswerOption;
import co.edu.unicauca.saberpro.domain.model.Question;
import co.edu.unicauca.saberpro.domain.model.QuestionStatus;
import co.edu.unicauca.saberpro.question.TestQuestions;
import co.edu.unicauca.saberpro.question.infrastructure.jpa.mapper.QuestionDomainMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({QuestionRepositoryJpaImpl.class, QuestionDomainMapper.class})
class QuestionRepositoryJpaImplTest {

    @Autowired
    QuestionRepositoryJpaImpl repository;

    @Autowired
    SpringQuestionDataRepository dataRepository;

    @Autowired
    TestEntityManager em;

    @Test
    void guardaLaPreguntaYSusOpcionesEnCascada() {
        Question saved = repository.save(TestQuestions.draft());

        assertThat(saved.getId()).isNotNull();
        assertThat(dataRepository.count()).isEqualTo(1);
        em.flush();
        long opciones = ((Number) em.getEntityManager()
                .createNativeQuery("select count(*) from answer_options").getSingleResult()).longValue();
        assertThat(opciones).isEqualTo(4);
    }

    @Test
    void buscaPorIdYDevuelveLasOpcionesEnOrden() {
        Long id = repository.save(TestQuestions.draft()).getId();
        em.clear();

        Question found = repository.findById(id).orElseThrow();

        assertThat(found.getOptions()).extracting(AnswerOption::text)
                .containsExactly("Microkernel", "Monolito", "Singleton", "Facade");
        assertThat(found.getStatus()).isEqualTo(QuestionStatus.DRAFT);
    }

    @Test
    void buscarUnIdQueNoExisteDevuelveVacio() {
        assertThat(repository.findById(999L)).isEmpty();
    }

    @Test
    void actualizarUnaPreguntaNoDuplicaLasOpciones() {
        Question saved = repository.save(TestQuestions.draft());
        em.flush();
        em.clear();

        Question pendiente = new QuestionBuilder()
                .id(saved.getId())
                .statement(saved.getStatement())
                .justification(saved.getJustification())
                .status(QuestionStatus.PENDING_REVIEW)
                .addOption("Microkernel", true).addOption("Monolito", false)
                .addOption("Singleton", false).addOption("Facade", false)
                .build();
        repository.save(pendiente);
        em.flush();
        em.clear();

        Question found = repository.findById(saved.getId()).orElseThrow();
        assertThat(found.getStatus()).isEqualTo(QuestionStatus.PENDING_REVIEW);
        assertThat(found.getOptions()).hasSize(4);
        assertThat(dataRepository.count()).isEqualTo(1);
    }
}
