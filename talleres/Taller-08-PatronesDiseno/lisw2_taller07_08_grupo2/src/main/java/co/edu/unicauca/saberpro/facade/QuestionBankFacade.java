package co.edu.unicauca.saberpro.facade;

import co.edu.unicauca.saberpro.adapter.ExternalQuestionAdapter;
import co.edu.unicauca.saberpro.adapter.ExternalQuestionService;
import co.edu.unicauca.saberpro.builder.InvalidQuestionException;
import co.edu.unicauca.saberpro.builder.QuestionBuilder;
import co.edu.unicauca.saberpro.builder.QuestionDirector;
import co.edu.unicauca.saberpro.domain.model.Competence;
import co.edu.unicauca.saberpro.domain.model.Difficulty;
import co.edu.unicauca.saberpro.domain.model.Question;
import co.edu.unicauca.saberpro.domain.repository.InMemoryQuestionRepository;
import co.edu.unicauca.saberpro.domain.service.QuestionService;
import co.edu.unicauca.saberpro.domain.validation.QuestionValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Patrón Facade (estructural): interfaz simplificada del banco de preguntas.
 *
 * <p>Oculta el repositorio, el validador, el servicio, el Builder y el Adapter. Quien la usa (por ahora
 * {@code ApplicationMain}) solo conoce cuatro operaciones: crear, importar, listar y buscar. Todos los errores salen
 * como {@link QuestionBankException}.
 */
public class QuestionBankFacade {

    private final QuestionService service;
    private final ExternalQuestionAdapter externalAdapter;

    /** Arma el banco con sus piezas por defecto: repositorio en memoria y servicio externo de ejemplo. */
    public QuestionBankFacade() {
        this(new QuestionService(new InMemoryQuestionRepository(), new QuestionValidator()),
                new ExternalQuestionAdapter(new ExternalQuestionService()));
    }

    /** Permite inyectar las piezas, por ejemplo un servicio externo distinto en las pruebas. */
    public QuestionBankFacade(QuestionService service, ExternalQuestionAdapter externalAdapter) {
        this.service = service;
        this.externalAdapter = externalAdapter;
    }

    /**
     * Crea una pregunta de selección múltiple: la arma con el Builder y la registra validándola.
     *
     * @param correctIndex posición (0 a 3) de la opción correcta dentro de {@code optionTexts}
     * @throws QuestionBankException si la pregunta está incompleta o no cumple las reglas del banco
     */
    public Question createQuestion(String context, String statement, List<String> optionTexts, int correctIndex,
                                   String justification, Competence competence, String topic, String subtopic,
                                   Difficulty difficulty) {
        try {
            Question question = new QuestionDirector(new QuestionBuilder()).buildMultipleChoice(context, statement,
                    optionTexts, correctIndex, justification, competence, topic, subtopic, difficulty);
            return service.register(question);
        } catch (InvalidQuestionException | IllegalArgumentException e) {
            throw new QuestionBankException(e.getMessage(), e);
        }
    }

    /**
     * Trae las preguntas del servicio externo y registra las que cumplen las reglas del banco.
     * Las que no se pueden convertir o validar no detienen la importación: quedan en el resumen con su motivo.
     */
    public ImportSummary importExternalQuestions() {
        List<Question> questions = externalAdapter.getQuestions();
        List<String> rejected = new ArrayList<>(externalAdapter.getRejected());
        int imported = 0;
        for (Question question : questions) {
            try {
                service.register(question);
                imported++;
            } catch (IllegalArgumentException e) {
                rejected.add("Pregunta externa descartada: " + e.getMessage());
            }
        }
        return new ImportSummary(imported, rejected);
    }

    public List<Question> listQuestions() {
        return service.findAll();
    }

    public Optional<Question> findQuestion(String id) {
        return service.findById(id);
    }
}
