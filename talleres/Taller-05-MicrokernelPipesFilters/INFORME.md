# Informe del Taller 5: Microkernel y Tuberías y Filtros

## 1. Resumen del proyecto de curso

El proyecto de curso es un Sistema para la Gestión, Validación y Administración de un Banco de Preguntas para la Preparación de las Pruebas Saber Pro, desarrollado de forma incremental a lo largo del semestre a través de los talleres del laboratorio. El sistema permite: autenticar usuarios con distintos roles (Autor de Preguntas, Revisor, Docente, Estudiante, Administrador); redactar preguntas y llevarlas a través de un ciclo de vida de 7 estados con revisión por pares; generar y presentar simulacros a partir de preguntas ya publicadas, con calificación automática.

El proyecto se organiza como un monolito modular (un único proceso desplegable, dividido en módulos Maven con límites explícitos), decisión tomada para facilitar una futura migración a microservicios. Este taller agrega un módulo nuevo, modulo-microkernel, que extiende el banco de preguntas con una arquitectura de plugins para poder incorporar nuevos tipos de pregunta sin modificar el núcleo del sistema.

## 2. Objetivo del taller

Implementar una solución al proyecto del Banco de Preguntas utilizando una arquitectura Microkernel (Plug-in), combinada con el patrón Tuberías y Filtros (Pipes and Filters), para permitir agregar nuevos tipos de pregunta (selección múltiple, casos de estudio, preguntas con recursos multimedia, importadas, generadas por IA, etc.) sin modificar el núcleo del sistema, validando el contenido de cada pregunta antes de que sea creada.

## 3. Arquitectura implementada

### 3.1 Patrón Microkernel

El núcleo, QuestionMicrokernel, administra el banco de preguntas y carga los plugins de forma dinámica por Reflexión (uso obligatorio, según el ejemplo visto en clase teórica sobre el envío de paquetes a distintos países), leyendo el archivo de configuración plugins.properties. A diferencia del ejemplo del taller (que trabaja con una entidad Question de juguete) , esta implementación reutiliza la entidad Question real del proyecto de curso (con su ciclo de vida de 7 estados, competencia, tema y dificultad), para que las preguntas generadas por los plugins queden integradas al mismo banco de preguntas que usan el Autor, el Revisor y el Docente, y no en un banco aislado.

**Contrato QuestionPlugin (cumplido estrictamente por los tres plugins):**

```
public interface QuestionPlugin {
    String getName();
    boolean supports(String type);
    Question generate(QuestionRequest request);
}
```

**Plugins implementados (mínimo exigido: 3):**

- MultipleChoiceQuestionPlugin: genera preguntas de selección múltiple (tipo "MULTIPLE_CHOICE").
- CaseQuestionPlugin: genera preguntas de análisis de casos (tipo "CASO"); antepone "Caso de estudio:" al enunciado.
- MultimediaQuestionPlugin: genera preguntas con recurso multimedia (tipo "MULTIMEDIA"); referencia una URL/ruta del recurso dentro del enunciado.

Los tres extienden una clase base común, BaseQuestionPlugin, que aplica el patrón Template Method: centraliza la ejecución del pipeline de validación y la construcción de la Question real (id, distractores, competencia, respuesta correcta), y delega en cada subclase únicamente cómo se redacta el enunciado final.

**Registro de plugins (plugins.properties), leído por Reflexión:**

```
plugin.1=co.unicauca.saberpro.microkernel.plugins.MultipleChoiceQuestionPlugin
plugin.2=co.unicauca.saberpro.microkernel.plugins.CaseQuestionPlugin
plugin.3=co.unicauca.saberpro.microkernel.plugins.MultimediaQuestionPlugin
```

Para agregar un plugin nuevo solo hace falta una línea en este archivo, el núcleo nunca conoce las clases concretas de los plugins, solo la interfaz QuestionPlugin, y las instancia con Class.forName(...).getDeclaredConstructor().newInstance().

### 3.2 Patrón Tuberías y Filtros

Antes de construir la pregunta, cada plugin ejecuta un pipeline de validación (QuestionPipeline) compuesto por cuatro filtros, cada uno con una única responsabilidad y encadenados de forma secuencial. El pipeline se detiene en el primer filtro que falla.

Los cuatro filtros exigidos por la guía del taller:

- ContentValidationFilter: valida que el título y el enunciado no estén vacíos.
- OptionsValidationFilter: valida que existan exactamente 4 opciones de respuesta, todas con contenido.
- ClassificationFilter: valida que la clasificación (competencia) sea una de las reconocidas por el banco real (Lectura crítica, Razonamiento cuantitativo, Competencias ciudadanas, Comunicación escrita, Inglés).
- CorrectAnswerValidationFilter: valida que la respuesta correcta esté presente entre las opciones.

```
public interface QuestionFilter {
    boolean process(QuestionRequest request);
}
```

La guía exige integrar el pipeline en al menos uno de los plugins; en esta implementación se integró en los tres, para garantizar que ninguna pregunta (sin importar el tipo) pueda crearse sin pasar por la misma validación.

## 4. Decisiones de diseño relevantes

- Se reutiliza la entidad Question real del proyecto de curso (con Competencia, Dificultad y el ciclo de vida de 7 estados) en lugar de crear una entidad de juguete aparte, para que este taller quede integrado al proyecto y no sea un ejercicio aislado.
- Cada pregunta generada se registra a través de QuestionService.registrarPreguntaGenerada(...) (no directamente contra el repositorio), para que el patrón Observer del proyecto siga funcionando: las vistas de estadísticas y gráfica se refrescan también cuando un plugin genera una pregunta.
- Los plugins se instancian por Reflexión con un constructor vacío (requisito del patrón), por lo que generan su propio identificador único (UUID) en vez de depender de un generador de ids inyectado.
- El módulo modulo-microkernel se integró como un módulo Maven independiente dentro del monolito modular del proyecto de curso, dependiendo únicamente de modulo-preguntas: consistente con la arquitectura general del sistema.

## 5. Estructura del módulo

```
modulo-microkernel/
├── src/main/java/co/unicauca/saberpro/microkernel/
│   ├── common/
│   │   ├── entities/QuestionRequest.java
│   │   └── interfaces/QuestionPlugin.java
│   ├── core/QuestionMicrokernel.java
│   ├── pipeline/
│   │   ├── base/QuestionFilter.java
│   │   ├── base/QuestionPipeline.java
│   │   └── filters/ (los 4 filtros)
│   ├── plugins/
│   │   ├── BaseQuestionPlugin.java
│   │   ├── MultipleChoiceQuestionPlugin.java
│   │   ├── CaseQuestionPlugin.java
│   │   └── MultimediaQuestionPlugin.java
│   └── presentation/GUIMicrokernel.java  (interfaz Swing)
├── src/main/resources/plugins.properties
└── src/test/java/... (34 pruebas)
```

La interfaz gráfica (GUIMicrokernel, Swing) se abre junto con la ventana del Autor de Preguntas: permite elegir el tipo de pregunta, diligenciar el formulario (título, enunciado, 4 opciones, respuesta correcta, competencia, tema, dificultad y, si aplica, el recurso multimedia) y generar la pregunta a través del microkernel.

## 6. Pruebas unitarias automatizadas

Se agregaron 34 pruebas nuevas (JUnit 5 + Mockito) para este taller, que se suman a las ya existentes del resto del proyecto:

| **Módulo** | **Clases de prueba** | **Pruebas** |
|---|---|---|
| modulo-microkernel (nuevo, Taller 5) | 9 | 34 |
| modulo-preguntas | 3 | 28 |
| modulo-simulacros | 4 | 25 |
| modulo-usuarios | 4 | 20 |
| Total | 20 | 107 |

Resultado de la ejecución completa: mvn test: 107/107 pruebas exitosas, 0 fallos, 0 errores.

## 7. Cómo compilar y ejecutar

```
mvn test      # ejecuta las 107 pruebas de todos los módulos
mvn package   # genera app/target/banco-preguntas-saberpro.jar
java -jar app/target/banco-preguntas-saberpro.jar
```

Al iniciar sesión con el usuario autor1 (rol Autor de Preguntas) se abren dos ventanas: el formulario habitual de redacción de preguntas y la nueva ventana del generador por plugins (Microkernel).

## 8. Repositorio

Código fuente completo, incluyendo este módulo y sus pruebas: https://github.com/DVLASZ/banco-preguntas-saberpro

## 9. Conclusiones

- El patrón Microkernel permitió agregar tres tipos de pregunta distintos sin modificar el núcleo del sistema ni el resto del proyecto: solo se agregaron clases nuevas y una línea de configuración por cada plugin.
- Combinar Microkernel con Tuberías y Filtros garantiza que la extensibilidad (nuevos tipos de pregunta) no sacrifique la calidad de los datos: toda pregunta, sin importar el plugin que la genere, pasa por las mismas cuatro validaciones.
- Integrar el taller directamente sobre el dominio real del proyecto de curso (en vez de un ejercicio aislado) permitió reutilizar el patrón Observer ya existente, de forma que las preguntas generadas por plugins son indistinguibles, para el resto del sistema, de las creadas manualmente.

