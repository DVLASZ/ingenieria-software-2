package co.edu.unicauca.saberpro;

import co.edu.unicauca.saberpro.model.Pregunta;
import co.edu.unicauca.saberpro.pipeline.FiltroCompetenciaOficial;
import co.edu.unicauca.saberpro.pipeline.FiltroEnunciado;
import co.edu.unicauca.saberpro.pipeline.FiltroLongitudEnunciado;
import co.edu.unicauca.saberpro.pipeline.ValidadorPipeline;
import co.edu.unicauca.saberpro.repository.IPreguntaRepository;
import co.edu.unicauca.saberpro.repository.PreguntaListRepository;
import co.edu.unicauca.saberpro.service.GestorPreguntaServiceImpl;
import co.edu.unicauca.saberpro.service.IPreguntaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class PreguntaServiceTest {
    private IPreguntaRepository repository;
    private IPreguntaService service;

    @BeforeEach
    void setUp() {
        repository = new PreguntaListRepository();
        ValidadorPipeline pipeline = new ValidadorPipeline();
        pipeline.addFiltro(new FiltroEnunciado());
        pipeline.addFiltro(new FiltroLongitudEnunciado());
        service = new GestorPreguntaServiceImpl(repository, pipeline);
    }

    private Pregunta pregunta(int id, String enunciado) {
        return new Pregunta(id, enunciado, Arrays.asList("A. Uno", "B. Dos"), "A. Uno", "BORRADOR", 50, "Lectura Crítica");
    }

    private Pregunta preguntaConCompetencia(String competencia) {
        return new Pregunta(1, "Enunciado suficientemente largo", Arrays.asList("A. Uno", "B. Dos"), "A. Uno", "BORRADOR", 50, competencia);
    }

    @Test
    void registraPreguntaValida() {
        assertTrue(service.registrarPregunta(pregunta(1, "¿Cuál es la función del patrón Microkernel?")));
        assertEquals(1, service.obtenerTodas().size());
    }

    @Test
    void rechazaEnunciadoCorto() {
        assertFalse(service.registrarPregunta(pregunta(1, "Muy corto")));
        assertTrue(service.obtenerTodas().isEmpty());
    }

    @Test
    void rechazaEnunciadoVacio() {
        assertFalse(service.registrarPregunta(pregunta(1, "   ")));
    }

    @Test
    void rechazaEnunciadoNulo() {
        assertFalse(service.registrarPregunta(pregunta(1, null)));
    }

    @Test
    void repositorioGuardaYBuscaPorId() {
        service.registrarPregunta(pregunta(7, "Enunciado suficientemente largo"));
        assertNotNull(repository.buscarPorId(7));
        assertNull(repository.buscarPorId(99));
    }

    // --- Opción 09: FiltroCompetenciaOficial ---

    @Test
    void filtroAceptaLasTresCompetenciasOficiales() {
        FiltroCompetenciaOficial filtro = new FiltroCompetenciaOficial();
        assertTrue(filtro.procesar(preguntaConCompetencia("Lectura Crítica")));
        assertTrue(filtro.procesar(preguntaConCompetencia("Razonamiento Cuantitativo")));
        assertTrue(filtro.procesar(preguntaConCompetencia("Inglés")));
    }

    @Test
    void filtroRechazaCompetenciaNoOficial() {
        assertFalse(new FiltroCompetenciaOficial().procesar(preguntaConCompetencia("Geografía")));
    }

    @Test
    void filtroRechazaCompetenciaNulaOVacia() {
        FiltroCompetenciaOficial filtro = new FiltroCompetenciaOficial();
        assertFalse(filtro.procesar(preguntaConCompetencia(null)));
        assertFalse(filtro.procesar(preguntaConCompetencia("")));
        assertFalse(filtro.procesar(null));
    }

    @Test
    void filtroEsSensibleAMayusculasYTildes() {
        FiltroCompetenciaOficial filtro = new FiltroCompetenciaOficial();
        assertFalse(filtro.procesar(preguntaConCompetencia("Ingles")));
        assertFalse(filtro.procesar(preguntaConCompetencia("lectura crítica")));
    }

    @Test
    void servicioRechazaPreguntaConCompetenciaInvalidaUsandoElPipeline() {
        ValidadorPipeline pipeline = new ValidadorPipeline();
        pipeline.addFiltro(new FiltroEnunciado());
        pipeline.addFiltro(new FiltroLongitudEnunciado());
        pipeline.addFiltro(new FiltroCompetenciaOficial());
        IPreguntaService servicio = new GestorPreguntaServiceImpl(repository, pipeline);

        assertFalse(servicio.registrarPregunta(preguntaConCompetencia("Geografía")));
        assertTrue(servicio.obtenerTodas().isEmpty());
        assertTrue(servicio.registrarPregunta(preguntaConCompetencia("Inglés")));
        assertEquals(1, servicio.obtenerTodas().size());
    }
}
