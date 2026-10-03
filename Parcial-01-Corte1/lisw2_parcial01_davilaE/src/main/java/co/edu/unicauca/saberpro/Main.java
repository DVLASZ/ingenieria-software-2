package co.edu.unicauca.saberpro;

import co.edu.unicauca.saberpro.model.Pregunta;
import co.edu.unicauca.saberpro.pipeline.*;
import co.edu.unicauca.saberpro.repository.*;
import co.edu.unicauca.saberpro.service.*;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== BANCO DE PREGUNTAS SABER PRO (GRUPO B) ===");

        IPreguntaRepository repository = new PreguntaListRepository();
        ValidadorPipeline pipeline = new ValidadorPipeline();
        pipeline.addFiltro(new FiltroEnunciado());
        pipeline.addFiltro(new FiltroLongitudEnunciado());
        pipeline.addFiltro(new FiltroCompetenciaOficial());

        // ERROR 4: Le pasaba null al servicio en vez del pipeline y se rompía con NullPointerException.
        // CORREGIDO: ahora le paso el pipeline.
        IPreguntaService service = new GestorPreguntaServiceImpl(repository, pipeline);

        Pregunta p1 = new Pregunta(
                1,
                "¿Cuál es la función principal del patrón Microkernel para agregar módulos sin alterar el núcleo?",
                Arrays.asList("A. Plugins dinámicos", "B. Monolito rígido", "C. Base de datos", "D. Redes"),
                "A. Plugins dinámicos",
                "BORRADOR",
                85,
                "Lectura Crítica"
        );

        // Pregunta con competencia inválida: el pipeline debe rechazarla
        Pregunta p2 = new Pregunta(
                2,
                "¿Cuál es la capital de Francia según el mapa político actual?",
                Arrays.asList("A. París", "B. Roma", "C. Madrid", "D. Berlín"),
                "A. París",
                "BORRADOR",
                60,
                "Geografía"
        );

        registrar(service, p1);
        registrar(service, p2);
    }

    private static void registrar(IPreguntaService service, Pregunta pregunta) {
        System.out.println("\nRegistrando pregunta " + pregunta.getId() + " (" + pregunta.getCompetencia() + ")...");
        boolean exito = service.registrarPregunta(pregunta);
        if (exito) {
            System.out.println(" Pregunta registrada con éxito. Total en banco: " + service.obtenerTodas().size());
        } else {
            System.out.println(" No se pudo registrar la pregunta debido a fallos en el pipeline.");
        }
    }
}
