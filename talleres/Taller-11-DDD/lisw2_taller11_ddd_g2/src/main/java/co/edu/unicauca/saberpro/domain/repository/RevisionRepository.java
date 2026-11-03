package co.edu.unicauca.saberpro.domain.repository;

import co.edu.unicauca.saberpro.domain.model.Revision;
import java.util.Optional;

/** Contrato para guardar y consultar revisiones. */
public interface RevisionRepository {

    /** Guarda la revisión; si es nueva le asigna su identidad. */
    Revision guardar(Revision revision);

    Optional<Revision> buscarPorId(Long id);

    Optional<Revision> buscarPorPregunta(Long preguntaId);
}
