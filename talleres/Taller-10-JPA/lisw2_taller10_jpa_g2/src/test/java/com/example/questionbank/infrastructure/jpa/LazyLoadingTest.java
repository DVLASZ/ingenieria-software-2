package com.example.questionbank.infrastructure.jpa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.questionbank.domain.model.Competency;
import com.example.questionbank.domain.model.Question;
import com.example.questionbank.domain.model.Role;
import com.example.questionbank.domain.model.Topic;
import com.example.questionbank.domain.model.User;
import com.example.questionbank.infrastructure.jpa.record.QuestionRecord;
import com.example.questionbank.infrastructure.jpa.repository.SpringQuestionDataRepository;
import java.util.List;
import org.hibernate.LazyInitializationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Shows what happens with lazy associations when nobody keeps a transaction open. The test runs without a
 * surrounding transaction and cleans the tables by hand.
 */
@DisplayName("Lazy associations outside a transaction")
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class LazyLoadingTest extends PersistenceTestSupport {

    @Autowired
    private SpringQuestionDataRepository data;

    @AfterEach
    void cleanTables() {
        for (String table : List.of("question_competencies", "quiz_questions", "question_metadata",
                "answer_options", "multiple_choice_questions", "true_false_questions", "questions", "quizzes",
                "topics", "competencies", "users")) {
            jdbc.execute("delete from " + table);
        }
    }

    private Long saveClassifiedQuestion() {
        User author = saveUser(Role.AUTHOR, "ada");
        Topic topic = saveTopic("Statistics");
        Competency competency = saveCompetency("QUANT");
        Question saved = questions.save(newTrueFalse(author, "Statement"));
        saved.classify(topic, List.of(competency));
        return questions.save(saved).getId();
    }

    @Test
    void theRecordCannotReadItsCompetenciesOnceTheTransactionEnded() {
        Long id = saveClassifiedQuestion();

        QuestionRecord record = data.findById(id).orElseThrow();

        assertThatThrownBy(() -> record.getCompetencies().size()).isInstanceOf(LazyInitializationException.class);
    }

    @Test
    void theRepositoryMapsInsideItsOwnTransactionSoTheDomainObjectIsComplete() {
        Long id = saveClassifiedQuestion();

        Question found = questions.findById(id).orElseThrow();

        assertThat(found.getCompetencies()).hasSize(1);
        assertThat(found.getTopic().getName()).isEqualTo("Statistics");
        assertThat(found.getAuthor().getUsername()).isEqualTo("ada");
    }
}
