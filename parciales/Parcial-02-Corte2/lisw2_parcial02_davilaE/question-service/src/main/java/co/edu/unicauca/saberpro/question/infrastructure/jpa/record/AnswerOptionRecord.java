package co.edu.unicauca.saberpro.question.infrastructure.jpa.record;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "answer_options")
public class AnswerOptionRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "option_text", nullable = false, length = 1000)
    private String text;

    @Column(name = "is_correct", nullable = false)
    private boolean correct;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id")
    private QuestionRecord question;

    protected AnswerOptionRecord() {
    }

    public AnswerOptionRecord(String text, boolean correct) {
        this.text = text;
        this.correct = correct;
    }

    public Long getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public boolean isCorrect() {
        return correct;
    }

    public QuestionRecord getQuestion() {
        return question;
    }

    void setQuestion(QuestionRecord question) {
        this.question = question;
    }
}
