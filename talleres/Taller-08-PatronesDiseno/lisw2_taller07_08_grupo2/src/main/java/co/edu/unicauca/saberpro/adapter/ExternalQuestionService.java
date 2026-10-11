package co.edu.unicauca.saberpro.adapter;

import java.util.List;

/**
 * Adaptable del patrón Adapter: simula un servicio de otra plataforma que entrega las preguntas como
 * cadenas crudas, con un formato propio que el banco no entiende.
 *
 * <p>Cada cadena tiene 9 campos separados por {@code |}:
 * <pre>
 * contexto | enunciado | opcion1;opcion2;opcion3;opcion4 | letra correcta | justificación | competencia | tema | subtema | dificultad
 * </pre>
 * La competencia y la dificultad se escriben con el nombre de las enumeraciones del modelo
 * (por ejemplo {@code CRITICAL_READING} y {@code BASIC}).
 */
public class ExternalQuestionService {

    private final List<String> rawQuestions;

    /** Servicio con tres preguntas de ejemplo, una de ellas con un error a propósito (solo tres opciones). */
    public ExternalQuestionService() {
        this(List.of(
                "Un equipo desarrolla una aplicación web que crece con el tiempo.|"
                        + "¿Qué principio recomienda depender de abstracciones y no de clases concretas?|"
                        + "Principio de inversión de dependencias;Principio de responsabilidad única;"
                        + "Principio abierto/cerrado;Principio de segregación de interfaces|A|"
                        + "La inversión de dependencias (DIP) indica que los módulos de alto nivel dependen de abstracciones.|"
                        + "CRITICAL_READING|Principios SOLID|DIP|INTERMEDIATE",
                "Una tienda necesita calcular el 15 % de descuento sobre un producto de 80 000 pesos.|"
                        + "¿Cuál es el valor final que paga el cliente?|"
                        + "68 000 pesos;65 000 pesos;72 000 pesos;12 000 pesos|A|"
                        + "El descuento es 12 000 pesos y 80 000 menos 12 000 es 68 000.|"
                        + "QUANTITATIVE_REASONING|Porcentajes|Descuentos|BASIC",
                "Una pregunta incompleta enviada por la otra plataforma.|"
                        + "¿Cuál de estas opciones es correcta?|"
                        + "Opción uno;Opción dos;Opción tres|B|"
                        + "No debería importarse porque solo trae tres opciones.|"
                        + "CITIZENSHIP|Constitución|Derechos|BASIC"));
    }

    /** Servicio con las cadenas que se le indiquen; sirve para las pruebas. */
    public ExternalQuestionService(List<String> rawQuestions) {
        this.rawQuestions = List.copyOf(rawQuestions);
    }

    /** Entrega las preguntas tal como las manda la otra plataforma. */
    public List<String> fetchRawQuestions() {
        return rawQuestions;
    }
}
