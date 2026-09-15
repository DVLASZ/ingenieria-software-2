# Taller 5 — Patrones Microkernel, Tuberías y Filtros

Laboratorio de Ingeniería de Software II — Universidad del Cauca (Periodo 2-2026)

A partir de este taller, el desarrollo ya no se hace como un ejercicio
suelto aparte: **todo el código vive directamente en el repositorio del
proyecto de curso**,
[`banco-preguntas-saberpro`](https://github.com/DVLASZ/banco-preguntas-saberpro),
dentro del módulo [`modulo-microkernel`](https://github.com/DVLASZ/banco-preguntas-saberpro/tree/main/modulo-microkernel).
Esta carpeta solo deja constancia de qué se hizo para este taller y del
enlace para revisarlo.

## Qué se implementó

- **Patrón Microkernel (Plug-in)**: `QuestionMicrokernel` administra el
  banco de preguntas y carga dinámicamente, por **Reflexión**, los
  plugins registrados en `plugins.properties` — igual que el ejemplo
  visto en clase del envío de paquetes a distintos países. Se
  implementaron 3 plugins (`MultipleChoiceQuestionPlugin`,
  `CaseQuestionPlugin`, `MultimediaQuestionPlugin`), todos sujetos al
  contrato `QuestionPlugin`.
- **Patrón Tuberías y Filtros**: pipeline de 4 filtros de validación
  (`ContentValidationFilter`, `OptionsValidationFilter`,
  `ClassificationFilter`, `CorrectAnswerValidationFilter`), integrado en
  los tres plugins antes de generar cualquier pregunta.
- Las preguntas generadas son la **entidad `Question` real** del
  proyecto de curso (no una de juguete aparte), por lo que quedan
  integradas al mismo banco que usan el Autor, el Revisor y el Docente.
- 34 pruebas unitarias nuevas (JUnit 5 + Mockito) para los filtros, el
  pipeline, los plugins y el núcleo.

## Dónde revisarlo

- Código y pruebas: [`banco-preguntas-saberpro/modulo-microkernel`](https://github.com/DVLASZ/banco-preguntas-saberpro/tree/main/modulo-microkernel)
- Detalle de arquitectura y decisiones de diseño: ver el `README.md` del
  módulo en ese mismo repositorio.

## Autores

Taller realizado en pareja:

- Edward Dávila — edwarddavila@unicauca.edu.co
- Laura Isabel Sánchez Fernández
