package com.example.questionbank.domain.model;

import com.example.questionbank.domain.exception.BusinessRuleException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * A question with several options and exactly one correct answer.
 */
public class MultipleChoiceQuestion extends Question {

    /** Correct options every question must have. */
    public static final int CORRECT_OPTIONS = 1;

    /** Wrong options every question must have. */
    public static final int DISTRACTORS = 4;

    private final List<AnswerOption> options = new ArrayList<>();

    /**
     * Creates a new draft.
     *
     * @param id identity, null until the repository saves it
     * @param statement what the question asks
     * @param difficulty how hard it is
     * @param author who writes it
     * @param options the options, correct and wrong ones
     */
    public MultipleChoiceQuestion(Long id, String statement, Difficulty difficulty, User author,
                                  List<AnswerOption> options) {
        super(id, statement, difficulty, author);
        this.options.addAll(Validation.requireNonNull(options, "options"));
    }

    /**
     * Rebuilds a question in any state, for example when it comes back from the database.
     *
     * @param id identity
     * @param statement what the question asks
     * @param difficulty how hard it is
     * @param status current status
     * @param author who wrote it
     * @param topic topic, may be null
     * @param competencies competencies it evaluates
     * @param metadata editorial data, may be null
     * @param options the options
     */
    public MultipleChoiceQuestion(Long id, String statement, Difficulty difficulty, QuestionStatus status,
                                  User author, Topic topic, Collection<Competency> competencies,
                                  QuestionMetadata metadata, List<AnswerOption> options) {
        super(id, statement, difficulty, status, author, topic, competencies, metadata);
        this.options.addAll(Validation.requireNonNull(options, "options"));
    }

    @Override
    public QuestionType getType() {
        return QuestionType.MULTIPLE_CHOICE;
    }

    /**
     * Replaces all the options. Only drafts can be edited.
     *
     * @param newOptions the new options
     */
    public void replaceOptions(List<AnswerOption> newOptions) {
        requireDraft("change the options of");
        List<AnswerOption> copy = new ArrayList<>(Validation.requireNonNull(newOptions, "options"));
        options.clear();
        options.addAll(copy);
    }

    /** {@return the options, as a read-only list} */
    public List<AnswerOption> getOptions() {
        return List.copyOf(options);
    }

    /**
     * Finds the right answer.
     *
     * @return the correct option
     */
    public AnswerOption getCorrectOption() {
        return options.stream().filter(AnswerOption::correct).findFirst()
                .orElseThrow(() -> new BusinessRuleException("The question has no correct option"));
    }

    @Override
    protected void validateContent() {
        long correct = options.stream().filter(AnswerOption::correct).count();
        if (correct != CORRECT_OPTIONS) {
            throw new BusinessRuleException(
                    "A multiple choice question needs exactly " + CORRECT_OPTIONS + " correct option");
        }
        long distractors = options.size() - correct;
        if (distractors != DISTRACTORS) {
            throw new BusinessRuleException(
                    "A multiple choice question needs exactly " + DISTRACTORS + " distractors");
        }
        Set<String> texts = new HashSet<>();
        for (AnswerOption option : options) {
            if (!texts.add(option.text().strip().toLowerCase(Locale.ROOT))) {
                throw new BusinessRuleException("Option texts must be different: " + option.text());
            }
        }
    }
}
