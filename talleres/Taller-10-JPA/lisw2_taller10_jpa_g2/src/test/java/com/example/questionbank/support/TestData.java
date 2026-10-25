package com.example.questionbank.support;

import com.example.questionbank.domain.model.AnswerOption;
import com.example.questionbank.domain.model.Competency;
import com.example.questionbank.domain.model.Difficulty;
import com.example.questionbank.domain.model.MultipleChoiceQuestion;
import com.example.questionbank.domain.model.Role;
import com.example.questionbank.domain.model.Topic;
import com.example.questionbank.domain.model.TrueFalseQuestion;
import com.example.questionbank.domain.model.User;
import java.util.List;

/** Small factory methods shared by the tests. */
public final class TestData {

    private TestData() {
    }

    /** Builds a user with the given role; the name is derived from the role. */
    public static User user(Role role) {
        String name = role.name().toLowerCase();
        return new User(null, name + "1", "Test " + name, name + "1@example.com", role);
    }

    /** Builds a topic without identity. */
    public static Topic topic() {
        return new Topic(null, "Argumentative texts");
    }

    /** Builds a competency without identity. */
    public static Competency competency(String code) {
        return new Competency(null, code, "Competency " + code);
    }

    /** Builds one correct option and four distractors. */
    public static List<AnswerOption> validOptions() {
        return List.of(
                new AnswerOption("The author defends a thesis", true),
                new AnswerOption("The author tells a story", false),
                new AnswerOption("The author lists data", false),
                new AnswerOption("The author asks a question", false),
                new AnswerOption("The author gives instructions", false));
    }

    /** Builds a valid multiple choice draft for the given author. */
    public static MultipleChoiceQuestion multipleChoice(User author) {
        return new MultipleChoiceQuestion(null, "What is the main idea of the text?", Difficulty.MEDIUM, author,
                validOptions());
    }

    /** Builds a true/false draft for the given author. */
    public static TrueFalseQuestion trueFalse(User author) {
        return new TrueFalseQuestion(null, "The text defends a thesis.", Difficulty.EASY, author, true);
    }
}
