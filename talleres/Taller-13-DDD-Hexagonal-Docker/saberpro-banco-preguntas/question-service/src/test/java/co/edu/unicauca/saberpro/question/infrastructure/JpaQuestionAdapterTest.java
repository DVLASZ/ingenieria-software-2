package co.edu.unicauca.saberpro.question.infrastructure;

import co.edu.unicauca.saberpro.question.QuestionServiceApplication;
import co.edu.unicauca.saberpro.question.domain.ports.outbound.PreguntaRepositoryPort;
import co.edu.unicauca.saberpro.question.infrastructure.jpa.JpaQuestionAdapter;
import co.edu.unicauca.saberpro.question.infrastructure.jpa.QuestionDomainMapper;
import co.edu.unicauca.saberpro.question.infrastructure.jpa.SpringQuestionDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;

@DataJpaTest
@ContextConfiguration(classes = QuestionServiceApplication.class)
class JpaQuestionAdapterTest extends PreguntaRepositoryPortContractTest {

    @Autowired
    SpringQuestionDataRepository repositorio;

    @Override
    PreguntaRepositoryPort puerto() {
        return new JpaQuestionAdapter(repositorio, new QuestionDomainMapper());
    }
}
