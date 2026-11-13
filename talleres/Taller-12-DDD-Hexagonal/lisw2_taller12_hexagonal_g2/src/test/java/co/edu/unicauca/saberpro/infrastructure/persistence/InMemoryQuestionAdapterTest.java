package co.edu.unicauca.saberpro.infrastructure.persistence;

import co.edu.unicauca.saberpro.domain.ports.outbound.PreguntaRepositoryPort;
import co.edu.unicauca.saberpro.infrastructure.adapter.out.persistence.memory.InMemoryQuestionAdapter;

class InMemoryQuestionAdapterTest extends PreguntaRepositoryPortContractTest {

    private final PreguntaRepositoryPort adaptador = new InMemoryQuestionAdapter();

    @Override
    PreguntaRepositoryPort puerto() {
        return adaptador;
    }
}
