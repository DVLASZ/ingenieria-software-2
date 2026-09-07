# Ingeniería de Software II

Repositorio de trabajos sueltos de la materia Ingeniería de Software II
(laboratorio y teoría). Cada carpeta es un trabajo independiente — taller,
parcial o ejercicio — con su propio `README.md` explicando de qué se
trata y cómo compilarlo/ejecutarlo.

> El **proyecto de curso** ("Sistema para la Gestión, Validación y
> Administración de un Banco de Preguntas para la Preparación de las
> Pruebas Saber Pro") ya tiene su propio repositorio aparte:
> [`banco-preguntas-saberpro`](https://github.com/DVLASZ/banco-preguntas-saberpro),
> ya que es un desarrollo continuo a lo largo del semestre y merece su
> propio historial de commits, issues, etc. Nació reorganizando el código
> del Taller 4 (que se conserva intacto aquí como entregable del
> laboratorio) como un **monolito modular**, para facilitar una futura
> migración a microservicios.

## Trabajos

- [`Taller-01-Polimorfismo/`](Taller-01-Polimorfismo) — jerarquía `Figure` /
  `Circle` / `Square` / `Triangle` para practicar polimorfismo en Java.
- [`Taller-02-SOLID/`](Taller-02-SOLID) — aplicación de escritorio
  (Swing + SQLite) para el módulo de gestión de usuarios del proyecto de
  curso, aplicando los 5 principios SOLID.
- [`Taller-03-C4/`](Taller-03-C4) — modelo de arquitectura C4 (Contexto,
  Contenedores, Componentes y Clases) del proyecto de curso, construido
  con la biblioteca oficial C4 de diagrams.net.
- [`Taller-04-CapasMVC/`](Taller-04-CapasMVC) — aplicación de escritorio
  (Swing) para el módulo de gestión de preguntas del proyecto de curso,
  aplicando arquitectura en capas, el micropatrón MVC y el patrón
  Observer. Integra también el login y los roles del Taller 2. Este
  entregable queda congelado tal cual se calificó; el desarrollo sigue
  en [`banco-preguntas-saberpro`](https://github.com/DVLASZ/banco-preguntas-saberpro).

## Entorno

Los trabajos de código de este repositorio están pensados para
compilarse con **Java 17+ y Maven** (cada uno es un proyecto Maven
independiente, con su propio `pom.xml`). Se puede abrir cada carpeta como
proyecto separado en IntelliJ IDEA, o compilar/testear por consola con
`mvn test` / `mvn package` desde dentro de cada una. Los trabajos de
modelado/documentación (como el Taller 3) no requieren compilación, solo
un lector de Markdown y, opcionalmente, [diagrams.net](https://app.diagrams.net/)
para abrir el archivo fuente `.drawio`. Cada carpeta indica en su propio
README los requisitos puntuales adicionales (por ejemplo, SQLite en el
Taller 2).

## Autor

Edward Dávila — edwarddavila@unicauca.edu.co
