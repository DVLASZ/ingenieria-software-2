package com.example.questionbank.infrastructure.jpa;

import com.example.questionbank.domain.model.Competency;
import com.example.questionbank.domain.model.Difficulty;
import com.example.questionbank.domain.model.Question;
import com.example.questionbank.domain.model.QuestionStatus;
import com.example.questionbank.domain.model.Role;
import com.example.questionbank.domain.model.Topic;
import com.example.questionbank.domain.model.TrueFalseQuestion;
import com.example.questionbank.domain.model.User;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Common setup for the persistence tests: an H2 database with the JPA repositories of the infrastructure.
 */
@DataJpaTest
@Import({UserRepositoryJpa.class, CompetencyRepositoryJpa.class, TopicRepositoryJpa.class,
        QuestionRepositoryJpa.class, QuizRepositoryJpa.class})
abstract class PersistenceTestSupport {

    @Autowired
    protected UserRepositoryJpa users;
    @Autowired
    protected CompetencyRepositoryJpa competencies;
    @Autowired
    protected TopicRepositoryJpa topics;
    @Autowired
    protected QuestionRepositoryJpa questions;
    @Autowired
    protected QuizRepositoryJpa quizzes;
    @Autowired
    protected TestEntityManager entityManager;
    @Autowired
    protected JdbcTemplate jdbc;

    /** Writes pending changes and empties the persistence context, so the next read goes to the database. */
    protected void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    protected User saveUser(Role role, String username) {
        return users.save(new User(null, username, "Test " + username, username + "@example.com", role));
    }

    protected Topic saveTopic(String name) {
        return topics.save(new Topic(null, name));
    }

    protected Competency saveCompetency(String code) {
        return competencies.save(new Competency(null, code, "Competency " + code));
    }

    protected TrueFalseQuestion newTrueFalse(User author, String statement) {
        return new TrueFalseQuestion(null, statement, Difficulty.EASY, author, true);
    }

    /** Saves a true/false question and takes it through the whole life cycle until it is published. */
    protected Question publishedQuestion(User author, Topic topic, Competency competency, String statement) {
        Question saved = questions.save(newTrueFalse(author, statement));
        saved.classify(topic, List.of(competency));
        saved.submitForReview();
        saved.moveTo(QuestionStatus.IN_REVIEW);
        saved.moveTo(QuestionStatus.APPROVED);
        saved.moveTo(QuestionStatus.PUBLISHED);
        return questions.save(saved);
    }

    protected int countRows(String table) {
        Integer count = jdbc.queryForObject("select count(*) from " + table, Integer.class);
        return count == null ? 0 : count;
    }
}
