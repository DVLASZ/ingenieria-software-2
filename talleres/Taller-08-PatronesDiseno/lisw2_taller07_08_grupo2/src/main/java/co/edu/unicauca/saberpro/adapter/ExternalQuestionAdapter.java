package co.edu.unicauca.saberpro.adapter;

import co.edu.unicauca.saberpro.builder.IQuestionBuilder;
import co.edu.unicauca.saberpro.builder.QuestionBuilder;
import co.edu.unicauca.saberpro.domain.model.Competence;
import co.edu.unicauca.saberpro.domain.model.Difficulty;
import co.edu.unicauca.saberpro.domain.model.Question;

import java.util.ArrayList;
import java.util.List;

/**
 * Patrón Adapter (estructural): convierte el formato crudo de {@link ExternalQuestionService} en la interfaz
 * {@link QuestionProvider} que entiende el banco de preguntas.
 *
 * <p>Usa el {@link QuestionBuilder} para armar cada pregunta, así que las reglas del banco (4 opciones y 1 correcta)
 * también se aplican a lo que llega de afuera. Las cadenas que no se pueden convertir se descartan y quedan
 * registradas en {@link #getRejected()}; no detienen la importación.
 */
public class ExternalQuestionAdapter implements QuestionProvider {

    private static final int FIELDS = 9;

    private final ExternalQuestionService service;
    private final List<String> rejected = new ArrayList<>();

    public ExternalQuestionAdapter(ExternalQuestionService service) {
        this.service = service;
    }

    @Override
    public List<Question> getQuestions() {
        rejected.clear();
        List<Question> questions = new ArrayList<>();
        List<String> raws = service.fetchRawQuestions();
        for (int i = 0; i < raws.size(); i++) {
            try {
                questions.add(convert(raws.get(i)));
            } catch (RuntimeException e) {
                rejected.add("Pregunta " + (i + 1) + " descartada: " + e.getMessage());
            }
        }
        return questions;
    }

    /** Motivos por los que se descartaron preguntas en la última llamada a {@link #getQuestions()}. */
    public List<String> getRejected() {
        return List.copyOf(rejected);
    }

    private Question convert(String raw) {
        if (raw == null) {
            throw new IllegalArgumentException("la cadena es nula");
        }
        String[] fields = raw.split("\\|", -1);
        if (fields.length != FIELDS) {
            throw new IllegalArgumentException("tiene " + fields.length + " campos y se esperaban " + FIELDS);
        }
        String[] options = fields[2].split(";", -1);
        int correct = correctIndex(fields[3]);

        IQuestionBuilder builder = new QuestionBuilder()
                .setContext(fields[0])
                .setStatement(fields[1]);
        for (int i = 0; i < options.length; i++) {
            builder.addOption(options[i], i == correct);
        }
        return builder.setJustification(fields[4])
                .setClassification(parse(Competence.class, fields[5]), fields[6], fields[7],
                        parse(Difficulty.class, fields[8]))
                .build();
    }

    private int correctIndex(String letter) {
        String value = letter.strip().toUpperCase();
        if (value.length() != 1 || value.charAt(0) < 'A' || value.charAt(0) > 'Z') {
            throw new IllegalArgumentException("la letra de la respuesta correcta no es válida: '" + letter + "'");
        }
        return value.charAt(0) - 'A';
    }

    private <E extends Enum<E>> E parse(Class<E> type, String name) {
        try {
            return Enum.valueOf(type, name.strip().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("valor desconocido para " + type.getSimpleName() + ": '" + name + "'");
        }
    }
}
