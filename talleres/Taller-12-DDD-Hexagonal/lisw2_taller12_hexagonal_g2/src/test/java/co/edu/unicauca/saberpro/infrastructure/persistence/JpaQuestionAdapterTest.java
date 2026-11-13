package co.edu.unicauca.saberpro.infrastructure.persistence;

import co.edu.unicauca.saberpro.domain.ports.outbound.PreguntaRepositoryPort;
import co.edu.unicauca.saberpro.infrastructure.SaberproApplication;
import co.edu.unicauca.saberpro.infrastructure.adapter.out.persistence.jpa.JpaQuestionAdapter;
import co.edu.unicauca.saberpro.infrastructure.adapter.out.persistence.jpa.SpringDataPreguntaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;

@DataJpaTest
@ContextConfiguration(classes = SaberproApplication.class)
class JpaQuestionAdapterTest extends PreguntaRepositoryPortContractTest {

    @Autowired
    SpringDataPreguntaRepository repositorio;

    @Override
    PreguntaRepositoryPort puerto() {
        return new JpaQuestionAdapter(repositorio);
    }
}
