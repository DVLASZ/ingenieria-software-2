package com.example.questionbank.application;

import com.example.questionbank.domain.exception.BusinessRuleException;
import com.example.questionbank.domain.model.Competency;
import com.example.questionbank.domain.model.Topic;
import com.example.questionbank.domain.repository.CompetencyRepository;
import com.example.questionbank.domain.repository.TopicRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use cases about the academic classification: competencies and topics.
 */
@Service
@Transactional
public class CatalogService {

    private final CompetencyRepository competencies;
    private final TopicRepository topics;

    /**
     * Creates the service.
     *
     * @param competencies the competency repository contract
     * @param topics the topic repository contract
     */
    public CatalogService(CompetencyRepository competencies, TopicRepository topics) {
        this.competencies = competencies;
        this.topics = topics;
    }

    /**
     * Creates a competency. The code must be free.
     *
     * @param code unique code
     * @param name readable name
     * @return the stored competency
     */
    public Competency createCompetency(String code, String name) {
        Competency competency = new Competency(null, code, name);
        if (competencies.findByCode(code).isPresent()) {
            throw new BusinessRuleException("The competency code is already used: " + code);
        }
        return competencies.save(competency);
    }

    /**
     * Creates a topic. The name must be free.
     *
     * @param name unique name
     * @return the stored topic
     */
    public Topic createTopic(String name) {
        Topic topic = new Topic(null, name);
        if (topics.findByName(name).isPresent()) {
            throw new BusinessRuleException("The topic already exists: " + name);
        }
        return topics.save(topic);
    }

    /**
     * Lists all competencies.
     *
     * @return every competency
     */
    @Transactional(readOnly = true)
    public List<Competency> listCompetencies() {
        return competencies.findAll();
    }

    /**
     * Lists all topics.
     *
     * @return every topic
     */
    @Transactional(readOnly = true)
    public List<Topic> listTopics() {
        return topics.findAll();
    }
}
