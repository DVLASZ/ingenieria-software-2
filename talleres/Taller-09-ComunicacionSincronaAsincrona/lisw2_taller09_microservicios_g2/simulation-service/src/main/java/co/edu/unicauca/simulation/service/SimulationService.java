package co.edu.unicauca.simulation.service;

import co.edu.unicauca.simulation.client.QuestionClient;
import co.edu.unicauca.simulation.controller.ApiException;
import co.edu.unicauca.simulation.dto.Dtos.PreguntaRemota;
import co.edu.unicauca.simulation.dto.Dtos.RespuestaItem;
import co.edu.unicauca.simulation.messaging.SimulationEventPublisher;
import co.edu.unicauca.simulation.model.CatalogQuestion;
import co.edu.unicauca.simulation.model.Simulacro;
import co.edu.unicauca.simulation.model.SimulacroEstado;
import co.edu.unicauca.simulation.repository.CatalogRepository;
import co.edu.unicauca.simulation.repository.SimulacroRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class SimulationService {

    private final CatalogRepository catalogo;
    private final SimulacroRepository simulacros;
    private final QuestionClient preguntas;
    private final SimulationEventPublisher eventos;

    public SimulationService(CatalogRepository catalogo, SimulacroRepository simulacros, QuestionClient preguntas,
                             SimulationEventPublisher eventos) {
        this.catalogo = catalogo;
        this.simulacros = simulacros;
        this.preguntas = preguntas;
        this.eventos = eventos;
    }

    /** Trae el contenido de la pregunta publicada y lo guarda en el catálogo propio (una pregunta repetida no se duplica). */
    public void agregarAlCatalogo(Long questionId) {
        if (questionId == null || catalogo.existsById(questionId)) {
            return;
        }
        PreguntaRemota p = preguntas.obtener(questionId);
        catalogo.save(new CatalogQuestion(questionId, p.titulo(), p.competencia(), p.contexto(), p.pregunta(), p.opciones(), p.respuestaCorrecta()));
    }

    public List<CatalogQuestion> catalogo() {
        return catalogo.findAllByOrderByQuestionIdAsc();
    }

    public List<CatalogQuestion> preguntasDe(Simulacro s) {
        return s.getPreguntaIds().stream().map(id -> catalogo.findById(id).orElseThrow()).toList();
    }

    public Simulacro crear(Long userId, int cantidad) {
        List<CatalogQuestion> disponibles = catalogo();
        if (disponibles.isEmpty()) {
            throw new ApiException(HttpStatus.CONFLICT, "El catálogo está vacío: todavía no hay preguntas publicadas");
        }
        if (cantidad > disponibles.size()) {
            throw new ApiException(HttpStatus.CONFLICT, "El catálogo solo tiene " + disponibles.size() + " preguntas publicadas");
        }
        return simulacros.save(new Simulacro(userId, disponibles.stream().limit(cantidad).map(CatalogQuestion::getQuestionId).toList()));
    }

    /** Califica las respuestas (las que faltan cuentan como incorrectas) y publica el resultado. */
    public Simulacro resolver(Long id, List<RespuestaItem> respuestas) {
        Simulacro s = simulacros.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "No existe el simulacro " + id));
        if (s.getEstado() == SimulacroEstado.RESUELTO) {
            throw new ApiException(HttpStatus.CONFLICT, "El simulacro " + id + " ya fue resuelto");
        }
        Map<Long, String> dadas = respuestas.stream().collect(Collectors.toMap(RespuestaItem::questionId, r -> r.respuesta().trim().toUpperCase(), (a, b) -> b));
        if (!s.getPreguntaIds().containsAll(dadas.keySet())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Hay respuestas de preguntas que no pertenecen al simulacro");
        }
        Map<Long, CatalogQuestion> preguntasDelSimulacro = preguntasDe(s).stream().collect(Collectors.toMap(CatalogQuestion::getQuestionId, Function.identity()));
        int correctas = 0;
        Map<String, Map<String, Integer>> porCompetencia = new HashMap<>();
        for (CatalogQuestion q : preguntasDelSimulacro.values()) {
            boolean acierto = q.getRespuestaCorrecta().equalsIgnoreCase(dadas.get(q.getQuestionId()));
            correctas += acierto ? 1 : 0;
            Map<String, Integer> c = porCompetencia.computeIfAbsent(String.valueOf(q.getCompetencia()), k -> new HashMap<>(Map.of("correct", 0, "total", 0)));
            c.merge("total", 1, Integer::sum);
            if (acierto) {
                c.merge("correct", 1, Integer::sum);
            }
        }
        int total = s.getPreguntaIds().size();
        double puntaje = Math.round(1000.0 * correctas / total) / 10.0;
        s.resolver(correctas, total, puntaje);
        simulacros.save(s);
        eventos.simulacroResuelto(s.getId(), s.getUserId(), correctas, total, puntaje, porCompetencia);
        return s;
    }
}
