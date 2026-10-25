package com.example.questionbank.domain.model;

import com.example.questionbank.domain.exception.BusinessRuleException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * A set of published questions that a teacher puts together.
 */
public class Quiz {

    private Long id;
    private final String title;
    private final User owner;
    private final List<Question> questions = new ArrayList<>();

    /**
     * Creates an empty quiz.
     *
     * @param id identity, null until the repository saves it
     * @param title the title
     * @param owner the teacher who owns the quiz
     */
    public Quiz(Long id, String title, User owner) {
        this.id = id;
        this.title = Validation.requireText(title, "title");
        Validation.requireNonNull(owner, "owner");
        if (owner.getRole() != Role.TEACHER) {
            throw new BusinessRuleException("Only teachers can own quizzes");
        }
        this.owner = owner;
    }

    /**
     * Rebuilds a quiz that comes back from the database. It does not check the status of the questions again,
     * because a published question may have been archived after it joined the quiz.
     *
     * @param id identity
     * @param title the title
     * @param owner the teacher who owns the quiz
     * @param questions the questions it already has
     */
    public Quiz(Long id, String title, User owner, Collection<Question> questions) {
        this.id = id;
        this.title = title;
        this.owner = owner;
        this.questions.addAll(questions);
    }

    /**
     * Adds a question. Only published questions are accepted, and only once.
     *
     * @param question the question to add
     */
    public void addQuestion(Question question) {
        Validation.requireNonNull(question, "question");
        if (!question.isPublished()) {
            throw new BusinessRuleException(
                    "Only published questions can be added to a quiz, but the question is " + question.getStatus());
        }
        boolean repeated = questions.stream().anyMatch(existing -> existing == question
                || (existing.getId() != null && existing.getId().equals(question.getId())));
        if (repeated) {
            throw new BusinessRuleException("The question is already in the quiz");
        }
        questions.add(question);
    }

    /** {@return the identity, or null when not saved yet} */
    public Long getId() {
        return id;
    }

    /**
     * Sets the identity once the repository has saved the quiz.
     *
     * @param id the identity
     */
    public void setId(Long id) {
        this.id = id;
    }

    /** {@return the title} */
    public String getTitle() {
        return title;
    }

    /** {@return the teacher who owns the quiz} */
    public User getOwner() {
        return owner;
    }

    /** {@return the questions, as a read-only list} */
    public List<Question> getQuestions() {
        return Collections.unmodifiableList(questions);
    }
}
