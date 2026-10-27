package com.example.questionbank.infrastructure.jpa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.questionbank.domain.model.Question;
import com.example.questionbank.domain.model.QuestionStatus;
import com.example.questionbank.domain.model.Role;
import com.example.questionbank.domain.model.User;
import com.example.questionbank.infrastructure.jpa.record.QuestionRecord;
import com.example.questionbank.infrastructure.jpa.repository.SpringQuestionDataRepository;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Two people change the same question at the same time: the second save must fail instead of overwriting the first.
 * It runs without a surrounding transaction, so each save is its own transaction.
 */
@DisplayName("Optimistic locking")
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class OptimisticLockingTest extends PersistenceTestSupport {

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

    @Test
    void theSecondConcurrentChangeIsRejected() {
        User author = saveUser(Role.AUTHOR, "ada");
        Question saved = questions.save(newTrueFalse(author, "Statement"));
        QuestionRecord firstCopy = data.findById(saved.getId()).orElseThrow();
        QuestionRecord secondCopy = data.findById(saved.getId()).orElseThrow();

        firstCopy.setStatus(QuestionStatus.ARCHIVED);
        data.saveAndFlush(firstCopy);
        secondCopy.setStatement("Changed from a stale copy");

        assertThatThrownBy(() -> data.saveAndFlush(secondCopy)).isInstanceOf(ObjectOptimisticLockingFailureException.class);
        assertThat(questions.findById(saved.getId()).orElseThrow().getStatus()).isEqualTo(QuestionStatus.ARCHIVED);
    }
}
