package com.example.questionbank.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.questionbank.domain.exception.BusinessRuleException;
import com.example.questionbank.domain.model.Competency;
import com.example.questionbank.domain.model.Role;
import com.example.questionbank.domain.model.Topic;
import com.example.questionbank.domain.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Users, competencies and topics")
class ReferenceModelTest {

    @Test
    void createsAValidUser() {
        User user = new User(null, "ada", "Ada Lovelace", "ada@example.com", Role.AUTHOR);

        assertThat(user.getUsername()).isEqualTo("ada");
        assertThat(user.getRole()).isEqualTo(Role.AUTHOR);
        assertThat(user.getId()).isNull();
    }

    @Test
    void rejectsABlankUsername() {
        assertThatThrownBy(() -> new User(null, " ", "Ada", "ada@example.com", Role.AUTHOR))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void rejectsAnEmailWithoutAtSign() {
        assertThatThrownBy(() -> new User(null, "ada", "Ada", "ada.example.com", Role.AUTHOR))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void rejectsAMissingRole() {
        assertThatThrownBy(() -> new User(null, "ada", "Ada", "ada@example.com", null))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void competencyNeedsCodeAndName() {
        assertThat(new Competency(null, "CRIT", "Critical reading").getCode()).isEqualTo("CRIT");
        assertThatThrownBy(() -> new Competency(null, "", "Critical reading"))
                .isInstanceOf(BusinessRuleException.class);
        assertThatThrownBy(() -> new Competency(null, "CRIT", null)).isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void topicNeedsAName() {
        assertThat(new Topic(null, "Statistics").getName()).isEqualTo("Statistics");
        assertThatThrownBy(() -> new Topic(null, " ")).isInstanceOf(BusinessRuleException.class);
    }
}
