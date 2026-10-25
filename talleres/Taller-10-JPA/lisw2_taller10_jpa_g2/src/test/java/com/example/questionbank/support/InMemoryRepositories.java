package com.example.questionbank.support;

import com.example.questionbank.domain.model.Competency;
import com.example.questionbank.domain.model.Question;
import com.example.questionbank.domain.model.QuestionStatus;
import com.example.questionbank.domain.model.Quiz;
import com.example.questionbank.domain.model.Topic;
import com.example.questionbank.domain.model.User;
import com.example.questionbank.domain.repository.CompetencyRepository;
import com.example.questionbank.domain.repository.QuestionRepository;
import com.example.questionbank.domain.repository.QuizRepository;
import com.example.questionbank.domain.repository.TopicRepository;
import com.example.questionbank.domain.repository.UserRepository;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Repositories that keep everything in a map. They implement the domain contracts, so the application services can
 * be tested without JPA, without Spring and without a database.
 */
public final class InMemoryRepositories {

    private InMemoryRepositories() {
    }

    /** In-memory users. */
    public static class Users implements UserRepository {
        private final Map<Long, User> store = new LinkedHashMap<>();
        private long next = 1;

        @Override
        public User save(User user) {
            if (user.getId() == null) {
                user.setId(next++);
            }
            store.put(user.getId(), user);
            return user;
        }

        @Override
        public Optional<User> findById(Long id) {
            return Optional.ofNullable(store.get(id));
        }

        @Override
        public Optional<User> findByUsername(String username) {
            return store.values().stream().filter(user -> user.getUsername().equals(username)).findFirst();
        }

        @Override
        public List<User> findAll() {
            return new ArrayList<>(store.values());
        }
    }

    /** In-memory competencies. */
    public static class Competencies implements CompetencyRepository {
        private final Map<Long, Competency> store = new LinkedHashMap<>();
        private long next = 1;

        @Override
        public Competency save(Competency competency) {
            if (competency.getId() == null) {
                competency.setId(next++);
            }
            store.put(competency.getId(), competency);
            return competency;
        }

        @Override
        public Optional<Competency> findById(Long id) {
            return Optional.ofNullable(store.get(id));
        }

        @Override
        public Optional<Competency> findByCode(String code) {
            return store.values().stream().filter(competency -> competency.getCode().equals(code)).findFirst();
        }

        @Override
        public List<Competency> findAllById(Collection<Long> ids) {
            return ids.stream().map(store::get).filter(java.util.Objects::nonNull).toList();
        }

        @Override
        public List<Competency> findAll() {
            return new ArrayList<>(store.values());
        }
    }

    /** In-memory topics. */
    public static class Topics implements TopicRepository {
        private final Map<Long, Topic> store = new LinkedHashMap<>();
        private long next = 1;

        @Override
        public Topic save(Topic topic) {
            if (topic.getId() == null) {
                topic.setId(next++);
            }
            store.put(topic.getId(), topic);
            return topic;
        }

        @Override
        public Optional<Topic> findById(Long id) {
            return Optional.ofNullable(store.get(id));
        }

        @Override
        public Optional<Topic> findByName(String name) {
            return store.values().stream().filter(topic -> topic.getName().equals(name)).findFirst();
        }

        @Override
        public List<Topic> findAll() {
            return new ArrayList<>(store.values());
        }
    }

    /** In-memory questions. */
    public static class Questions implements QuestionRepository {
        private final Map<Long, Question> store = new LinkedHashMap<>();
        private long next = 1;

        @Override
        public Question save(Question question) {
            if (question.getId() == null) {
                question.setId(next++);
            }
            store.put(question.getId(), question);
            return question;
        }

        @Override
        public Optional<Question> findById(Long id) {
            return Optional.ofNullable(store.get(id));
        }

        @Override
        public List<Question> findAllById(Collection<Long> ids) {
            return ids.stream().map(store::get).filter(java.util.Objects::nonNull).toList();
        }

        @Override
        public List<Question> findAll() {
            return new ArrayList<>(store.values());
        }

        @Override
        public List<Question> findByAuthorId(Long authorId) {
            return store.values().stream().filter(question -> authorId.equals(question.getAuthor().getId())).toList();
        }

        @Override
        public List<Question> findByStatus(QuestionStatus status) {
            return store.values().stream().filter(question -> question.getStatus() == status).toList();
        }

        @Override
        public List<Question> findPublishedByCompetencyId(Long competencyId) {
            return store.values().stream().filter(Question::isPublished)
                    .filter(question -> question.getCompetencies().stream()
                            .anyMatch(competency -> competencyId.equals(competency.getId())))
                    .toList();
        }
    }

    /** In-memory quizzes. */
    public static class Quizzes implements QuizRepository {
        private final Map<Long, Quiz> store = new LinkedHashMap<>();
        private long next = 1;

        @Override
        public Quiz save(Quiz quiz) {
            if (quiz.getId() == null) {
                quiz.setId(next++);
            }
            store.put(quiz.getId(), quiz);
            return quiz;
        }

        @Override
        public Optional<Quiz> findById(Long id) {
            return Optional.ofNullable(store.get(id));
        }

        @Override
        public List<Quiz> findAll() {
            return new ArrayList<>(store.values());
        }
    }
}
