package com.example.questionbank.api;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

/**
 * Runs the whole application on H2 and talks to it over HTTP. There is no transaction around the test, so every
 * request commits for real and the next one reads from the database; the tables are emptied after each test.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Question bank API, end to end")
class QuestionBankApiTest {

    private static final String FIVE_OPTIONS = """
            [{"text":"Right","correct":true},{"text":"Wrong 1","correct":false},{"text":"Wrong 2","correct":false},
             {"text":"Wrong 3","correct":false},{"text":"Wrong 4","correct":false}]""";

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private JdbcTemplate jdbc;

    @AfterEach
    void cleanTables() {
        for (String table : java.util.List.of("question_competencies", "quiz_questions", "question_metadata",
                "answer_options", "multiple_choice_questions", "true_false_questions", "questions", "quizzes",
                "topics", "competencies", "users")) {
            jdbc.execute("delete from " + table);
        }
    }

    private ResultActions send(String url, String body) throws Exception {
        return mvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(body));
    }

    private long idOf(ResultActions result) throws Exception {
        JsonNode json = mapper.readTree(result.andReturn().getResponse().getContentAsString());
        return json.get("id").asLong();
    }

    private long createUser(String username, String role) throws Exception {
        return idOf(send("/api/users", """
                {"username":"%s","fullName":"Full %s","email":"%s@example.com","role":"%s"}"""
                .formatted(username, username, username, role)).andExpect(status().isCreated()));
    }

    private long createTopic(String name) throws Exception {
        return idOf(send("/api/topics", "{\"name\":\"%s\"}".formatted(name)).andExpect(status().isCreated()));
    }

    private long createCompetency(String code) throws Exception {
        return idOf(send("/api/competencies", "{\"code\":\"%s\",\"name\":\"Competency %s\"}".formatted(code, code))
                .andExpect(status().isCreated()));
    }

    private long createMultipleChoice(long authorId) throws Exception {
        return idOf(send("/api/questions/multiple-choice", """
                {"statement":"What is the main idea?","difficulty":"MEDIUM","authorId":%d,"options":%s,
                 "metadata":{"cognitiveLevel":"Analysis","estimatedSeconds":90}}"""
                .formatted(authorId, FIVE_OPTIONS)).andExpect(status().isCreated()));
    }

    private void classify(long questionId, long topicId, long competencyId) throws Exception {
        mvc.perform(put("/api/questions/" + questionId + "/classification").contentType(MediaType.APPLICATION_JSON)
                .content("{\"topicId\":%d,\"competencyIds\":[%d]}".formatted(topicId, competencyId)))
                .andExpect(status().isOk());
    }

    private ResultActions moveTo(long questionId, String target) throws Exception {
        return send("/api/questions/" + questionId + "/status", "{\"target\":\"%s\"}".formatted(target));
    }

    private long publishedQuestion(long authorId, long topicId, long competencyId) throws Exception {
        long id = createMultipleChoice(authorId);
        classify(id, topicId, competencyId);
        mvc.perform(post("/api/questions/" + id + "/submit")).andExpect(status().isOk());
        moveTo(id, "IN_REVIEW").andExpect(status().isOk());
        moveTo(id, "APPROVED").andExpect(status().isOk());
        moveTo(id, "PUBLISHED").andExpect(status().isOk());
        return id;
    }

    @Test
    void goesFromADraftToAQuizThroughTheWholeLifeCycle() throws Exception {
        long author = createUser("ada", "AUTHOR");
        long teacher = createUser("grace", "TEACHER");
        long topic = createTopic("Argumentative texts");
        long competency = createCompetency("CRIT");

        long question = createMultipleChoice(author);
        mvc.perform(get("/api/questions/" + question)).andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("MULTIPLE_CHOICE"))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.options", hasSize(5)))
                .andExpect(jsonPath("$.metadata.estimatedSeconds").value(90));
        classify(question, topic, competency);
        mvc.perform(post("/api/questions/" + question + "/submit")).andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING_REVIEW"));
        moveTo(question, "IN_REVIEW").andExpect(status().isOk());
        moveTo(question, "APPROVED").andExpect(status().isOk());
        moveTo(question, "PUBLISHED").andExpect(status().isOk()).andExpect(jsonPath("$.status").value("PUBLISHED"));

        mvc.perform(get("/api/questions/published").param("competencyId", String.valueOf(competency)))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(question));
        long quiz = idOf(send("/api/quizzes", "{\"title\":\"Midterm\",\"ownerId\":%d,\"questionIds\":[%d]}"
                .formatted(teacher, question)).andExpect(status().isCreated()));
        mvc.perform(get("/api/quizzes/" + quiz)).andExpect(status().isOk())
                .andExpect(jsonPath("$.questionIds[0]").value(question));
    }

    @Test
    void createsATrueFalseQuestion() throws Exception {
        long author = createUser("ada", "AUTHOR");

        send("/api/questions/true-false", "{\"statement\":\"The earth is round.\",\"difficulty\":\"EASY\","
                + "\"authorId\":%d,\"expectedAnswer\":true}".formatted(author))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.type").value("TRUE_FALSE"))
                .andExpect(jsonPath("$.expectedAnswer").value(true))
                .andExpect(jsonPath("$.options").doesNotExist());
    }

    @Test
    void filtersTheListByAuthorAndStatus() throws Exception {
        long ada = createUser("ada", "AUTHOR");
        long alan = createUser("alan", "AUTHOR");
        createMultipleChoice(ada);
        createMultipleChoice(alan);

        mvc.perform(get("/api/questions")).andExpect(jsonPath("$", hasSize(2)));
        mvc.perform(get("/api/questions").param("authorId", String.valueOf(ada))).andExpect(jsonPath("$", hasSize(1)));
        mvc.perform(get("/api/questions").param("status", "DRAFT")).andExpect(jsonPath("$", hasSize(2)));
        mvc.perform(get("/api/questions").param("status", "PUBLISHED")).andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void aQuizRefusesQuestionsThatAreNotPublished() throws Exception {
        long author = createUser("ada", "AUTHOR");
        long teacher = createUser("grace", "TEACHER");
        long draft = createMultipleChoice(author);

        send("/api/quizzes", "{\"title\":\"Midterm\",\"ownerId\":%d,\"questionIds\":[%d]}".formatted(teacher, draft))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.detail").value(
                        org.hamcrest.Matchers.containsString("published")));
        mvc.perform(get("/api/quizzes")).andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void refusesAnInvalidTransitionAndKeepsTheStatus() throws Exception {
        long author = createUser("ada", "AUTHOR");
        long question = createMultipleChoice(author);

        moveTo(question, "PUBLISHED").andExpect(status().isConflict());
        mvc.perform(get("/api/questions/" + question)).andExpect(jsonPath("$.status").value("DRAFT"));
    }

    @Test
    void refusesToSubmitAQuestionThatIsNotClassified() throws Exception {
        long author = createUser("ada", "AUTHOR");
        long question = createMultipleChoice(author);

        mvc.perform(post("/api/questions/" + question + "/submit")).andExpect(status().isConflict());
    }

    @Test
    void refusesToSubmitAMultipleChoiceWithTwoCorrectOptions() throws Exception {
        long author = createUser("ada", "AUTHOR");
        long topic = createTopic("Topic");
        long competency = createCompetency("CRIT");
        long question = idOf(send("/api/questions/multiple-choice", """
                {"statement":"Two right answers","difficulty":"HARD","authorId":%d,"options":
                [{"text":"A","correct":true},{"text":"B","correct":true},{"text":"C","correct":false},
                 {"text":"D","correct":false},{"text":"E","correct":false}]}""".formatted(author))
                .andExpect(status().isCreated()));
        classify(question, topic, competency);

        mvc.perform(post("/api/questions/" + question + "/submit")).andExpect(status().isConflict());
    }

    @Test
    void answers404ForAnAuthorOrAQuestionThatDoesNotExist() throws Exception {
        send("/api/questions/true-false", "{\"statement\":\"Statement\",\"difficulty\":\"EASY\","
                + "\"authorId\":9999,\"expectedAnswer\":true}").andExpect(status().isNotFound());
        mvc.perform(get("/api/questions/9999")).andExpect(status().isNotFound());
    }

    @Test
    void answers400ForAnInvalidBody() throws Exception {
        send("/api/questions/true-false", "{\"statement\":\" \",\"difficulty\":\"EASY\",\"authorId\":1,"
                + "\"expectedAnswer\":true}").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.statement").exists());
        send("/api/questions/true-false", "{\"statement\":\"Statement\",\"difficulty\":\"IMPOSSIBLE\","
                + "\"authorId\":1,\"expectedAnswer\":true}").andExpect(status().isBadRequest());
    }

    @Test
    void refusesRepeatedUsernamesAndEmails() throws Exception {
        createUser("ada", "AUTHOR");

        send("/api/users", "{\"username\":\"ada\",\"fullName\":\"Other\",\"email\":\"other@example.com\","
                + "\"role\":\"AUTHOR\"}").andExpect(status().isConflict());
        send("/api/users", "{\"username\":\"other\",\"fullName\":\"Other\",\"email\":\"ada@example.com\","
                + "\"role\":\"AUTHOR\"}").andExpect(status().isConflict());
    }

    @Test
    void listsOnlyThePublishedQuestionsOfTheRequestedCompetency() throws Exception {
        long author = createUser("ada", "AUTHOR");
        long topic = createTopic("Topic");
        long critical = createCompetency("CRIT");
        long quantitative = createCompetency("QUANT");
        long published = publishedQuestion(author, topic, critical);
        publishedQuestion(author, topic, quantitative);
        long draft = createMultipleChoice(author);
        classify(draft, topic, critical);

        mvc.perform(get("/api/questions/published").param("competencyId", String.valueOf(critical)))
                .andExpect(jsonPath("$", hasSize(1))).andExpect(jsonPath("$[0].id").value(published));
        mvc.perform(get("/api/questions/published").param("competencyId", "9999")).andExpect(status().isNotFound());
    }

    @Test
    void reclassifiesADraftAndTheOldCompetenciesAreDropped() throws Exception {
        long author = createUser("ada", "AUTHOR");
        long topic = createTopic("Topic");
        long otherTopic = createTopic("Other topic");
        long critical = createCompetency("CRIT");
        long quantitative = createCompetency("QUANT");
        long question = createMultipleChoice(author);
        classify(question, topic, critical);

        classify(question, otherTopic, quantitative);

        mvc.perform(get("/api/questions/" + question)).andExpect(jsonPath("$.topicId").value(otherTopic))
                .andExpect(jsonPath("$.competencyIds", hasSize(1)))
                .andExpect(jsonPath("$.competencyIds[0]").value(quantitative));
    }

    @Test
    void classifyAnswers404400And409() throws Exception {
        long author = createUser("ada", "AUTHOR");
        long topic = createTopic("Topic");
        long competency = createCompetency("CRIT");
        long question = createMultipleChoice(author);

        mvc.perform(put("/api/questions/" + question + "/classification").contentType(MediaType.APPLICATION_JSON)
                .content("{\"topicId\":9999,\"competencyIds\":[%d]}".formatted(competency)))
                .andExpect(status().isNotFound());
        mvc.perform(put("/api/questions/" + question + "/classification").contentType(MediaType.APPLICATION_JSON)
                .content("{\"topicId\":%d,\"competencyIds\":[9999]}".formatted(topic)))
                .andExpect(status().isNotFound());
        mvc.perform(put("/api/questions/" + question + "/classification").contentType(MediaType.APPLICATION_JSON)
                .content("{\"topicId\":%d,\"competencyIds\":[]}".formatted(topic)))
                .andExpect(status().isBadRequest());
        mvc.perform(put("/api/questions/" + question + "/classification").contentType(MediaType.APPLICATION_JSON)
                .content("{\"topicId\":%d,\"competencyIds\":[null]}".formatted(topic)))
                .andExpect(status().isBadRequest());

        classify(question, topic, competency);
        mvc.perform(post("/api/questions/" + question + "/submit")).andExpect(status().isOk());
        mvc.perform(put("/api/questions/" + question + "/classification").contentType(MediaType.APPLICATION_JSON)
                .content("{\"topicId\":%d,\"competencyIds\":[%d]}".formatted(topic, competency)))
                .andExpect(status().isConflict());
    }

    @Test
    void refusesRepeatedCompetencyCodesAndTopicNames() throws Exception {
        createCompetency("CRIT");
        createTopic("Topic");

        send("/api/competencies", "{\"code\":\"CRIT\",\"name\":\"Another\"}").andExpect(status().isConflict());
        send("/api/topics", "{\"name\":\"Topic\"}").andExpect(status().isConflict());
    }

    @Test
    void aQuizRefusesANonTeacherOwnerARepeatedQuestionAndAnUnknownQuiz() throws Exception {
        long author = createUser("ada", "AUTHOR");
        long teacher = createUser("grace", "TEACHER");
        long topic = createTopic("Topic");
        long competency = createCompetency("CRIT");
        long published = publishedQuestion(author, topic, competency);

        send("/api/quizzes", "{\"title\":\"Midterm\",\"ownerId\":%d,\"questionIds\":[%d]}"
                .formatted(author, published)).andExpect(status().isConflict());
        send("/api/quizzes", "{\"title\":\"Midterm\",\"ownerId\":%d,\"questionIds\":[%d,%d]}"
                .formatted(teacher, published, published)).andExpect(status().isConflict());
        mvc.perform(get("/api/quizzes/9999")).andExpect(status().isNotFound());
    }

    @Test
    void answers400ForNullElementsAndForTextsThatAreTooLong() throws Exception {
        long author = createUser("ada", "AUTHOR");

        send("/api/questions/multiple-choice", "{\"statement\":\"Statement\",\"difficulty\":\"EASY\","
                + "\"authorId\":%d,\"options\":[null]}".formatted(author)).andExpect(status().isBadRequest());
        send("/api/questions/true-false", ("{\"statement\":\"%s\",\"difficulty\":\"EASY\",\"authorId\":%d,"
                + "\"expectedAnswer\":true}").formatted("x".repeat(2001), author))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.fields.statement").exists());
    }
}
