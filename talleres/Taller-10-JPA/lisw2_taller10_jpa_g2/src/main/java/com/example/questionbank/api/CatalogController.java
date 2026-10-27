package com.example.questionbank.api;

import com.example.questionbank.api.dto.CatalogDtos.CompetencyResponse;
import com.example.questionbank.api.dto.CatalogDtos.CreateCompetencyRequest;
import com.example.questionbank.api.dto.CatalogDtos.CreateTopicRequest;
import com.example.questionbank.api.dto.CatalogDtos.TopicResponse;
import com.example.questionbank.application.CatalogService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * HTTP endpoints for competencies and topics.
 */
@RestController
public class CatalogController {

    private final CatalogService service;

    /**
     * Creates the controller.
     *
     * @param service the catalog use cases
     */
    public CatalogController(CatalogService service) {
        this.service = service;
    }

    /**
     * Creates a competency.
     *
     * @param request the data of the competency
     * @return the stored competency
     */
    @PostMapping("/api/competencies")
    @ResponseStatus(HttpStatus.CREATED)
    public CompetencyResponse createCompetency(@Valid @RequestBody CreateCompetencyRequest request) {
        return CompetencyResponse.from(service.createCompetency(request.code(), request.name()));
    }

    /**
     * Lists all competencies.
     *
     * @return every competency
     */
    @GetMapping("/api/competencies")
    public List<CompetencyResponse> listCompetencies() {
        return service.listCompetencies().stream().map(CompetencyResponse::from).toList();
    }

    /**
     * Creates a topic.
     *
     * @param request the data of the topic
     * @return the stored topic
     */
    @PostMapping("/api/topics")
    @ResponseStatus(HttpStatus.CREATED)
    public TopicResponse createTopic(@Valid @RequestBody CreateTopicRequest request) {
        return TopicResponse.from(service.createTopic(request.name()));
    }

    /**
     * Lists all topics.
     *
     * @return every topic
     */
    @GetMapping("/api/topics")
    public List<TopicResponse> listTopics() {
        return service.listTopics().stream().map(TopicResponse::from).toList();
    }
}
