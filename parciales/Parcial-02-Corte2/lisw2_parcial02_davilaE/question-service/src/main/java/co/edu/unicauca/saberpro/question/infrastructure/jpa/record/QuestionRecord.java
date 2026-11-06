package co.edu.unicauca.saberpro.question.infrastructure.jpa.record;

import co.edu.unicauca.saberpro.domain.model.QuestionStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "questions")
public class QuestionRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "statement_text", nullable = false, length = 2000)
    private String statement;

    @Column(name = "justification_text", length = 2000)
    private String justification;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private QuestionStatus status;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<AnswerOptionRecord> options = new ArrayList<>();

    protected QuestionRecord() {
    }

    public QuestionRecord(Long id, String statement, String justification, QuestionStatus status) {
        this.id = id;
        this.statement = statement;
        this.justification = justification;
        this.status = status;
    }

    public void addOption(AnswerOptionRecord option) {
        option.setQuestion(this);
        options.add(option);
    }

    public Long getId() {
        return id;
    }

    public String getStatement() {
        return statement;
    }

    public String getJustification() {
        return justification;
    }

    public QuestionStatus getStatus() {
        return status;
    }

    public List<AnswerOptionRecord> getOptions() {
        return options;
    }
}
