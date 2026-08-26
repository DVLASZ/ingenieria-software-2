# Ingeniería de Software II

Repositorio de trabajos sueltos de la materia Ingeniería de Software II
(laboratorio y teoría). Cada carpeta es un trabajo independiente — taller,
parcial o ejercicio — con su propio `README.md` explicando de qué se
trata y cómo compilarlo/ejecutarlo.

> El **proyecto de curso** ("Sistema para la Gestión, Validación y
> Administración de un Banco de Preguntas para la Preparación de las
> Pruebas Saber Pro") tiene su propio repositorio aparte, ya que es un
> desarrollo continuo a lo largo del semestre y merece su propio historial
> de commits, issues, etc.

## Trabajos

- [`Taller-01-Polimorfismo/`](Taller-01-Polimorfismo) — jerarquía `Figure` /
  `Circle` / `Square` / `Triangle` para practicar polimorfismo en Java.
- [`Taller-02-SOLID/`](Taller-02-SOLID) — aplicación de escritorio
  (Swing + SQLite) para el módulo de gestión de usuarios del proyecto de
  curso, aplicando los 5 principios SOLID.

## Entorno

El código de este repositorio está pensado para compilarse con **Java 17+
y Maven** (cada carpeta es un proyecto Maven independiente, con su propio
`pom.xml`). Se puede abrir cada carpeta como proyecto separado en
IntelliJ IDEA, o compilar/testear por consola con `mvn test` /
`mvn package` desde dentro de cada una. Cada carpeta indica en su propio
README los requisitos puntuales adicionales (por ejemplo, SQLite en el
Taller 2).

## Autor

Edward Dávila — edwarddavilas36@gmail.com
