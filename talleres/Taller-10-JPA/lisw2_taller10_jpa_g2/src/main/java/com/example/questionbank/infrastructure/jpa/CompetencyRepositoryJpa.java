package com.example.questionbank.infrastructure.jpa;

import com.example.questionbank.domain.exception.NotFoundException;
import com.example.questionbank.domain.model.Competency;
import com.example.questionbank.domain.repository.CompetencyRepository;
import com.example.questionbank.infrastructure.jpa.mapper.CompetencyMapper;
import com.example.questionbank.infrastructure.jpa.repository.SpringCompetencyDataRepository;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Stores competencies with Spring Data JPA.
 */
@Repository
@Transactional(readOnly = true)
public class CompetencyRepositoryJpa implements CompetencyRepository {

    private final SpringCompetencyDataRepository data;

    /**
     * Creates the repository.
     *
     * @param data the Spring Data interface for competency records
     */
    public CompetencyRepositoryJpa(SpringCompetencyDataRepository data) {
        this.data = data;
    }

    /**
     * Inserts a new competency. Competencies are immutable in the domain, so saving one that already has
     * identity only checks that it exists.
     */
    @Override
    @Transactional
    public Competency save(Competency competency) {
        if (competency.getId() == null) {
            return CompetencyMapper.toDomain(data.save(CompetencyMapper.toRecord(competency)));
        }
        return findById(competency.getId())
                .orElseThrow(() -> new NotFoundException("Competency not found: " + competency.getId()));
    }

    @Override
    public Optional<Competency> findById(Long id) {
        return data.findById(id).map(CompetencyMapper::toDomain);
    }

    @Override
    public Optional<Competency> findByCode(String code) {
        return data.findByCode(code).map(CompetencyMapper::toDomain);
    }

    @Override
    public List<Competency> findAllById(Collection<Long> ids) {
        return data.findAllById(ids).stream().map(CompetencyMapper::toDomain).toList();
    }

    @Override
    public List<Competency> findAll() {
        return data.findAll(Sort.by("id")).stream().map(CompetencyMapper::toDomain).toList();
    }
}
