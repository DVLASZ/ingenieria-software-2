package com.example.questionbank.infrastructure.jpa.record;

import com.example.questionbank.domain.model.Difficulty;
import com.example.questionbank.domain.model.QuestionStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import org.hibernate.annotations.BatchSize;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA mapping of a multiple choice question: the options live in their own table.
 */
@Entity
@Table(name = "multiple_choice_questions")
public class MultipleChoiceQuestionRecord extends QuestionRecord {

    /** One question to many options; the options hold the foreign key and die with the question. */
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    @BatchSize(size = 50)
    private List<AnswerOptionRecord> options = new ArrayList<>();

    /** Required by JPA. */
    protected MultipleChoiceQuestionRecord() {
    }

    /**
     * Creates a record to be inserted.
     *
     * @param statement what the question asks
     * @param difficulty how hard it is
     * @param status current status
     * @param author record of the author
     */
    public MultipleChoiceQuestionRecord(String statement, Difficulty difficulty, QuestionStatus status,
                                        UserRecord author) {
        super(statement, difficulty, status, author);
    }

    /** {@return the option records, in insertion order} */
    public List<AnswerOptionRecord> getOptions() {
        return options;
    }

    /**
     * Adds an option and keeps both sides of the association in sync.
     *
     * @param option the option record
     */
    public void addOption(AnswerOptionRecord option) {
        options.add(option);
        option.setQuestion(this);
    }

    /** Removes all the options; orphan removal deletes their rows. */
    public void clearOptions() {
        options.clear();
    }
}
