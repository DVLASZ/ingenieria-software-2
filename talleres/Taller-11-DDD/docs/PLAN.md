# Plan del Taller 11

## Qué pide el enunciado

- Parte I: los cuatro contextos delimitados (Autoría, Validación, Ciclo de Vida y Revisión por Pares) con su lenguaje ubicuo.
- Parte II: objetos de valor inmutables (`Enunciado`, `Justificacion`, `OpcionRespuesta`), entidades (`Revisor`, `Revision`), el agregado
  `Pregunta` como raíz y sus invariantes: 4 opciones, 1 clave, justificación de al menos 15 caracteres, opciones solo editables en borrador.
- Parte III: el código sugerido y una prueba. Las pruebas deben comprobar que romper una regla lanza `ReglaNegocioException`.
- Entrega: repositorio con el dominio en Java puro (sin Spring ni JPA) y el informe `lisw2_taller11_g2.docx` con el enlace al repositorio.

## Qué decidimos

- Ampliar el mínimo con `Revision`, `Revisor`, `Asignacion`, `Evaluacion` y `Dictamen`, porque el contexto de revisión por pares también
  tiene reglas de negocio (de 1 a 3 revisores, el autor no revisa lo suyo, unanimidad) y es lo que se reutiliza en el proyecto de curso.
- Respetar los nombres, los paquetes y el español del enunciado.
- Objetos de valor como `record`: son inmutables y se comparan por sus atributos sin escribir más código.
- `Pregunta` y `Revision` son dos agregados distintos y se refieren por identificador, no por objeto.
- Un solo módulo Maven con JUnit 5 como única dependencia, y una prueba que revisa los imports del dominio.

## Qué corregimos del código de ejemplo

| Del enunciado | Qué hicimos |
|---|---|
| `Pregunta` no tenía forma de recibir el enunciado ni la justificación | Se agregaron `redactarEnunciado` y `justificar`, y se exigen para enviar |
| `Justificacion` no revisaba palabras prohibidas | Se usa `FiltroExpresion` (todas o ninguna de las anteriores) |
| `ReglaNegocioException` sin padre definido | Extiende `DomainException` |
| `OpcionRespuesta::isEsCorrecta` | Es un `record`, así que el acceso es `esCorrecta()` |
| Se podía agregar una opción repetida | Se rechaza si solo cambian mayúsculas, tildes o espacios |
| No había transiciones más allá de enviar | `EstadoPregunta.puedePasarA` define todo el ciclo y `Pregunta` lo respeta |

## Calendario

Del 2 al 5 de noviembre, unos dos commits por día, con el informe en Markdown antes de pasarlo a Word. Todo en local hasta que se diga.
