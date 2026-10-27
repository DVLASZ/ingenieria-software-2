package com.example.questionbank.infrastructure.jpa;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.questionbank.domain.model.Competency;
import com.example.questionbank.domain.model.Role;
import com.example.questionbank.domain.model.Topic;
import com.example.questionbank.domain.model.User;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("User, competency and topic repositories")
class ReferenceRepositoriesTest extends PersistenceTestSupport {

    @Test
    void savesAndFindsUsers() {
        User saved = saveUser(Role.REVIEWER, "linus");
        flushAndClear();

        assertThat(saved.getId()).isNotNull();
        assertThat(users.findById(saved.getId())).get().extracting(User::getRole).isEqualTo(Role.REVIEWER);
        assertThat(users.findByUsername("linus")).isPresent();
        assertThat(users.findByUsername("nobody")).isEmpty();
        assertThat(users.findAll()).hasSize(1);
    }

    @Test
    void savingAUserThatAlreadyHasIdentityDoesNotInsertAgain() {
        User saved = saveUser(Role.AUTHOR, "ada");

        users.save(saved);

        assertThat(countRows("users")).isEqualTo(1);
    }

    @Test
    void savesAndFindsCompetencies() {
        Competency critical = saveCompetency("CRIT");
        Competency quantitative = saveCompetency("QUANT");
        flushAndClear();

        assertThat(competencies.findByCode("CRIT")).get().extracting(Competency::getName)
                .isEqualTo("Competency CRIT");
        assertThat(competencies.findAllById(List.of(critical.getId(), quantitative.getId(), 9999L))).hasSize(2);
        assertThat(competencies.findAll()).hasSize(2);
    }

    @Test
    void savesAndFindsTopics() {
        Topic saved = saveTopic("Statistics");
        flushAndClear();

        assertThat(topics.findById(saved.getId())).get().extracting(Topic::getName).isEqualTo("Statistics");
        assertThat(topics.findByName("Statistics")).isPresent();
        assertThat(topics.findAll()).hasSize(1);
    }
}
