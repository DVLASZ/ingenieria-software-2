package com.example.questionbank.infrastructure.jpa.mapper;

import com.example.questionbank.domain.model.AnswerOption;
import com.example.questionbank.domain.model.Competency;
import com.example.questionbank.domain.model.MultipleChoiceQuestion;
import com.example.questionbank.domain.model.Question;
import com.example.questionbank.domain.model.QuestionMetadata;
import com.example.questionbank.domain.model.Topic;
import com.example.questionbank.domain.model.TrueFalseQuestion;
import com.example.questionbank.domain.model.User;
import com.example.questionbank.infrastructure.jpa.record.AnswerOptionRecord;
import com.example.questionbank.infrastructure.jpa.record.CompetencyRecord;
import com.example.questionbank.infrastructure.jpa.record.MultipleChoiceQuestionRecord;
import com.example.questionbank.infrastructure.jpa.record.QuestionMetadataRecord;
import com.example.questionbank.infrastructure.jpa.record.QuestionRecord;
import com.example.questionbank.infrastructure.jpa.record.TopicRecord;
import com.example.questionbank.infrastructure.jpa.record.TrueFalseQuestionRecord;
import com.example.questionbank.infrastructure.jpa.record.UserRecord;
import java.util.Collection;
import java.util.List;

/**
 * Converts questions between the domain hierarchy and the JPA hierarchy. It must run inside a transaction,
 * because it reads lazy associations.
 */
public final class QuestionMapper {

    private QuestionMapper() {
    }

    /**
     * Builds the domain question that matches the type of the record.
     *
     * @param record a question record, with its associations still loadable
     * @return the domain question
     */
    public static Question toDomain(QuestionRecord record) {
        User author = UserMapper.toDomain(record.getAuthor());
        Topic topic = record.getTopic() == null ? null : TopicMapper.toDomain(record.getTopic());
        List<Competency> competencies = record.getCompetencies().stream().map(CompetencyMapper::toDomain).toList();
        QuestionMetadata metadata = toDomain(record.getMetadata());
        if (record instanceof MultipleChoiceQuestionRecord multipleChoice) {
            List<AnswerOption> options = multipleChoice.getOptions().stream()
                    .map(option -> new AnswerOption(option.getText(), option.isCorrect())).toList();
            return new MultipleChoiceQuestion(record.getId(), record.getStatement(), record.getDifficulty(),
                    record.getStatus(), author, topic, competencies, metadata, options);
        }
        if (record instanceof TrueFalseQuestionRecord trueFalse) {
            return new TrueFalseQuestion(record.getId(), record.getStatement(), record.getDifficulty(),
                    record.getStatus(), author, topic, competencies, metadata, trueFalse.isExpectedAnswer());
        }
        throw new IllegalArgumentException("Unknown question record: " + record.getClass().getName());
    }

    /**
     * Builds a new record of the right subtype from a domain question.
     *
     * @param question the domain question
     * @param author record of the author, already stored
     * @param topic record of the topic, or null
     * @param competencies records of the competencies, already stored
     * @return a record ready to be inserted
     */
    public static QuestionRecord toNewRecord(Question question, UserRecord author, TopicRecord topic,
                                             Collection<CompetencyRecord> competencies) {
        QuestionRecord record;
        if (question instanceof MultipleChoiceQuestion multipleChoice) {
            MultipleChoiceQuestionRecord created = new MultipleChoiceQuestionRecord(question.getStatement(),
                    question.getDifficulty(), question.getStatus(), author);
            multipleChoice.getOptions().forEach(option ->
                    created.addOption(new AnswerOptionRecord(option.text(), option.correct())));
            record = created;
        } else if (question instanceof TrueFalseQuestion trueFalse) {
            record = new TrueFalseQuestionRecord(question.getStatement(), question.getDifficulty(),
                    question.getStatus(), author, trueFalse.isExpectedAnswer());
        } else {
            throw new IllegalArgumentException("Unknown question type: " + question.getClass().getName());
        }
        record.setTopic(topic);
        record.getCompetencies().addAll(competencies);
        applyMetadata(record, question.getMetadata());
        return record;
    }

    /**
     * Copies the state of a domain question into the record that is already stored.
     * Options are only replaced when they changed, so unchanged rows are not rewritten.
     *
     * @param record the stored record
     * @param question the domain question with the new state
     * @param topic record of the topic, or null
     * @param competencies records of the competencies
     */
    public static void copyInto(QuestionRecord record, Question question, TopicRecord topic,
                                Collection<CompetencyRecord> competencies) {
        record.setStatement(question.getStatement());
        record.setStatus(question.getStatus());
        record.setTopic(topic);
        record.getCompetencies().clear();
        record.getCompetencies().addAll(competencies);
        applyMetadata(record, question.getMetadata());
        if (record instanceof MultipleChoiceQuestionRecord multipleChoice
                && question instanceof MultipleChoiceQuestion domain) {
            if (!sameOptions(multipleChoice, domain)) {
                multipleChoice.clearOptions();
                domain.getOptions().forEach(option ->
                        multipleChoice.addOption(new AnswerOptionRecord(option.text(), option.correct())));
            }
        } else if (record instanceof MultipleChoiceQuestionRecord || question instanceof MultipleChoiceQuestion) {
            throw new IllegalStateException("The question type cannot change once it is stored");
        }
    }

    private static boolean sameOptions(MultipleChoiceQuestionRecord record, MultipleChoiceQuestion question) {
        List<AnswerOption> stored = record.getOptions().stream()
                .map(option -> new AnswerOption(option.getText(), option.isCorrect())).toList();
        return stored.equals(question.getOptions());
    }

    private static QuestionMetadata toDomain(QuestionMetadataRecord record) {
        if (record == null) {
            return null;
        }
        return new QuestionMetadata(record.getCognitiveLevel(), record.getEstimatedSeconds(),
                record.getEditorialNotes(), record.getLastReviewedAt());
    }

    private static void applyMetadata(QuestionRecord record, QuestionMetadata metadata) {
        if (metadata == null) {
            record.setMetadata(null);
        } else if (record.getMetadata() == null) {
            record.setMetadata(new QuestionMetadataRecord(record, metadata.cognitiveLevel(),
                    metadata.estimatedSeconds(), metadata.editorialNotes(), metadata.lastReviewedAt()));
        } else {
            record.getMetadata().update(metadata.cognitiveLevel(), metadata.estimatedSeconds(),
                    metadata.editorialNotes(), metadata.lastReviewedAt());
        }
    }
}
