package com.example.questionbank.api.dto;

import com.example.questionbank.domain.model.Competency;
import com.example.questionbank.domain.model.Topic;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request and response objects for competencies and topics.
 */
public final class CatalogDtos {

    private CatalogDtos() {
    }

    /**
     * Body to create a competency.
     *
     * @param code unique code
     * @param name readable name
     */
    public record CreateCompetencyRequest(@NotBlank @Size(max = 40) String code,
                                            @NotBlank @Size(max = 120) String name) {
    }

    /**
     * A competency as the API shows it.
     *
     * @param id identity
     * @param code unique code
     * @param name readable name
     */
    public record CompetencyResponse(Long id, String code, String name) {

        /**
         * Builds the response from a domain competency.
         *
         * @param competency the domain competency
         * @return the response
         */
        public static CompetencyResponse from(Competency competency) {
            return new CompetencyResponse(competency.getId(), competency.getCode(), competency.getName());
        }
    }

    /**
     * Body to create a topic.
     *
     * @param name unique name
     */
    public record CreateTopicRequest(@NotBlank @Size(max = 120) String name) {
    }

    /**
     * A topic as the API shows it.
     *
     * @param id identity
     * @param name unique name
     */
    public record TopicResponse(Long id, String name) {

        /**
         * Builds the response from a domain topic.
         *
         * @param topic the domain topic
         * @return the response
         */
        public static TopicResponse from(Topic topic) {
            return new TopicResponse(topic.getId(), topic.getName());
        }
    }
}
