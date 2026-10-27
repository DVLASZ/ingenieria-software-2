package com.example.questionbank.infrastructure.jpa;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.questionbank.domain.model.Competency;
import com.example.questionbank.domain.model.Question;
import com.example.questionbank.domain.model.QuestionMetadata;
import com.example.questionbank.domain.model.Role;
import com.example.questionbank.domain.model.Topic;
import com.example.questionbank.domain.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

/**
 * Checks that the database itself refuses invalid relations, even when the code does not check them.
 */
@DisplayName("Foreign keys and constraints")
class PersistenceConstraintsTest extends PersistenceTestSupport {

    @Test
    void anOptionCannotPointToAQuestionThatDoesNotExist() {
        assertThatThrownBy(() -> jdbc.update(
                "insert into answer_options (question_id, text, correct) values (?, ?, ?)", 9999L, "Orphan", true))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void aQuestionCannotHaveTwoMetadataRows() {
        User author = saveUser(Role.AUTHOR, "ada");
        Question saved = questions.save(newTrueFalse(author, "Statement"));
        saved.attachMetadata(new QuestionMetadata("Analysis", 60, null, null));
        questions.save(saved);
        flushAndClear();

        assertThatThrownBy(() -> jdbc.update(
                "insert into question_metadata (question_id, cognitive_level, estimated_seconds) values (?, ?, ?)",
                saved.getId(), "Synthesis", 30)).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void metadataCannotExistWithoutAQuestion() {
        assertThatThrownBy(() -> jdbc.update(
                "insert into question_metadata (question_id, cognitive_level, estimated_seconds) values (?, ?, ?)",
                9999L, "Analysis", 30)).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void aQuestionNeedsAnAuthor() {
        assertThatThrownBy(() -> jdbc.update(
                "insert into questions (statement, difficulty, status, author_id) values (?, ?, ?, ?)",
                "No author", "EASY", "DRAFT", null)).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void aQuestionCannotPointToAnAuthorThatDoesNotExist() {
        assertThatThrownBy(() -> jdbc.update(
                "insert into questions (statement, difficulty, status, author_id) values (?, ?, ?, ?)",
                "Ghost author", "EASY", "DRAFT", 9999L)).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void theJoinTableOnlyAcceptsExistingCompetencies() {
        User author = saveUser(Role.AUTHOR, "ada");
        Question saved = questions.save(newTrueFalse(author, "Statement"));
        flushAndClear();

        assertThatThrownBy(() -> jdbc.update(
                "insert into question_competencies (question_id, competency_id) values (?, ?)", saved.getId(), 9999L))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void usernamesAreUnique() {
        saveUser(Role.AUTHOR, "ada");

        assertThatThrownBy(() -> users.save(new User(null, "ada", "Other Ada", "other@example.com", Role.AUTHOR)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void emailsAreUnique() {
        saveUser(Role.AUTHOR, "ada");

        assertThatThrownBy(() -> users.save(new User(null, "other", "Other", "ada@example.com", Role.AUTHOR)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void competencyCodesAndTopicNamesAreUnique() {
        saveCompetency("CRIT");
        saveTopic("Statistics");

        assertThatThrownBy(() -> competencies.save(new Competency(null, "CRIT", "Another name")))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> topics.save(new Topic(null, "Statistics")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
