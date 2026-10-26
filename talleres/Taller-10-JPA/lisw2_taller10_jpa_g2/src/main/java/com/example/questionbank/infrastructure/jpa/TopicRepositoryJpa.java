package com.example.questionbank.infrastructure.jpa;

import com.example.questionbank.domain.exception.NotFoundException;
import com.example.questionbank.domain.model.Topic;
import com.example.questionbank.domain.repository.TopicRepository;
import com.example.questionbank.infrastructure.jpa.mapper.TopicMapper;
import com.example.questionbank.infrastructure.jpa.repository.SpringTopicDataRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Stores topics with Spring Data JPA.
 */
@Repository
@Transactional(readOnly = true)
public class TopicRepositoryJpa implements TopicRepository {

    private final SpringTopicDataRepository data;

    /**
     * Creates the repository.
     *
     * @param data the Spring Data interface for topic records
     */
    public TopicRepositoryJpa(SpringTopicDataRepository data) {
        this.data = data;
    }

    /**
     * Inserts a new topic. Topics are immutable in the domain, so saving one that already has identity only
     * checks that it exists.
     */
    @Override
    @Transactional
    public Topic save(Topic topic) {
        if (topic.getId() == null) {
            return TopicMapper.toDomain(data.save(TopicMapper.toRecord(topic)));
        }
        return findById(topic.getId()).orElseThrow(() -> new NotFoundException("Topic not found: " + topic.getId()));
    }

    @Override
    public Optional<Topic> findById(Long id) {
        return data.findById(id).map(TopicMapper::toDomain);
    }

    @Override
    public Optional<Topic> findByName(String name) {
        return data.findByName(name).map(TopicMapper::toDomain);
    }

    @Override
    public List<Topic> findAll() {
        return data.findAll(Sort.by("id")).stream().map(TopicMapper::toDomain).toList();
    }
}
