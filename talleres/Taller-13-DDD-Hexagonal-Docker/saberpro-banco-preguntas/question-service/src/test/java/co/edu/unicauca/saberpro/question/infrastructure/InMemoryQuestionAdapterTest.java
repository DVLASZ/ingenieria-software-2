package co.edu.unicauca.saberpro.question.infrastructure;

import co.edu.unicauca.saberpro.question.domain.ports.outbound.PreguntaRepositoryPort;
import co.edu.unicauca.saberpro.question.infrastructure.memory.InMemoryQuestionAdapter;

class InMemoryQuestionAdapterTest extends PreguntaRepositoryPortContractTest {

    private final PreguntaRepositoryPort adaptador = new InMemoryQuestionAdapter();

    @Override
    PreguntaRepositoryPort puerto() {
        return adaptador;
    }
}
