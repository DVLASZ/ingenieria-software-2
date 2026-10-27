package com.example.questionbank.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.questionbank.application.command.CreateUserCommand;
import com.example.questionbank.domain.exception.BusinessRuleException;
import com.example.questionbank.domain.exception.NotFoundException;
import com.example.questionbank.domain.model.Role;
import com.example.questionbank.domain.model.User;
import com.example.questionbank.support.InMemoryRepositories;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("User and catalog use cases")
class ReferenceServicesTest {

    private final UserService userService = new UserService(new InMemoryRepositories.Users());
    private final CatalogService catalog =
            new CatalogService(new InMemoryRepositories.Competencies(), new InMemoryRepositories.Topics());

    @Test
    void createsAndFindsUsers() {
        User created = userService.create(new CreateUserCommand("ada", "Ada Lovelace", "ada@example.com", Role.AUTHOR));

        assertThat(created.getId()).isNotNull();
        assertThat(userService.findById(created.getId())).isSameAs(created);
        assertThat(userService.list()).containsExactly(created);
    }

    @Test
    void refusesARepeatedUsername() {
        userService.create(new CreateUserCommand("ada", "Ada", "ada@example.com", Role.AUTHOR));

        assertThatThrownBy(() -> userService.create(new CreateUserCommand("ada", "Other", "other@example.com",
                Role.AUTHOR))).isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void anUnknownUserIsNotFound() {
        assertThatThrownBy(() -> userService.findById(1L)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void createsCompetenciesAndTopicsWithoutRepeating() {
        catalog.createCompetency("CRIT", "Critical reading");
        catalog.createTopic("Statistics");

        assertThat(catalog.listCompetencies()).hasSize(1);
        assertThat(catalog.listTopics()).hasSize(1);
        assertThatThrownBy(() -> catalog.createCompetency("CRIT", "Another")).isInstanceOf(BusinessRuleException.class);
        assertThatThrownBy(() -> catalog.createTopic("Statistics")).isInstanceOf(BusinessRuleException.class);
    }
}
