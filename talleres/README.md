# Talleres

Talleres del laboratorio de Ingeniería de Software II. Cada carpeta es un trabajo independiente con su propio
`README.md`, que explica de qué trata y cómo compilarlo o ejecutarlo.

| Taller | Tema | Dónde está el código | Informe |
|---|---|---|---|
| [Taller 1](Taller-01-Polimorfismo) | Polimorfismo: jerarquía `Figure`, `Circle`, `Square` y `Triangle` | En esta carpeta | |
| [Taller 2](Taller-02-SOLID) | Aplicación de escritorio (Swing y SQLite) para la gestión de usuarios, aplicando los cinco principios SOLID | En esta carpeta | [`INFORME.md`](Taller-02-SOLID/INFORME.md) |
| [Taller 3](Taller-03-C4) | Modelo de arquitectura C4 (contexto, contenedores, componentes y clases) del proyecto de curso | Diagramas en esta carpeta | [`INFORME.md`](Taller-03-C4/INFORME.md) |
| [Taller 4](Taller-04-CapasMVC) | Arquitectura en capas, MVC y Observer sobre el banco de preguntas, con el login y los roles del Taller 2 | En esta carpeta, congelado tal como se calificó | |
| [Taller 5](Taller-05-MicrokernelPipesFilters) | Microkernel y Tuberías y Filtros: generación de preguntas por plugins | En el proyecto, etiqueta [`taller-5`](https://github.com/DVLASZ/banco-preguntas-saberpro/tree/taller-5/modulo-microkernel) | |
| [Taller 6](Taller-06-ApiRestSpringBoot) | API REST con Spring Boot y JPA: CRUD de preguntas | En el proyecto, etiqueta [`taller-6`](https://github.com/DVLASZ/banco-preguntas-saberpro/tree/taller-6/modulo-api-rest) | |

## Criterio para ubicar el código

- Los ejercicios con su propio modelo o una estructura impuesta por la guía viven aquí, en su propia carpeta.
- Los que se aplican directamente sobre el banco de preguntas viven en el proyecto
  [`banco-preguntas-saberpro`](https://github.com/DVLASZ/banco-preguntas-saberpro) y aquí queda una carpeta con el
  README y el enlace a la versión entregada, fijada con una etiqueta (`taller-5`, `taller-6`).

## Entorno

Los talleres con código son proyectos Maven independientes (Java 17 o superior). Cada uno se abre como proyecto
propio en IntelliJ IDEA o se compila con `mvn test` o `mvn package` desde su carpeta.
