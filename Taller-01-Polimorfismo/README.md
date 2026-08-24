# Taller 1 - Polimorfismo

Laboratorio de Ingeniería de Software II — Universidad del Cauca (Periodo 2-2026)

## Descripción

Implementación de la jerarquía de figuras geométricas propuesta en la guía del
taller (`Figure` → `Circle`, `Square`, `Triangle`) para ilustrar el uso del
polimorfismo en Java: un mismo mensaje (`calculateArea()`, `calculatePerimeter()`)
enviado a objetos de distintas subclases produce comportamientos diferentes,
sin que el código cliente conozca el tipo concreto de cada figura.

## Estructura del proyecto

```
src/main/java/co/unicauca/polimorfismo/
├── Main.java                    # Aplicación cliente (demuestra el polimorfismo)
└── figuras/
    ├── Figure.java               # Clase abstracta base
    ├── Circle.java
    ├── Square.java
    └── Triangle.java
src/test/java/co/unicauca/polimorfismo/figuras/
    ├── CircleTest.java
    ├── SquareTest.java
    └── TriangleTest.java
```

## Notas de diseño

- `Figure` es una clase abstracta con los métodos abstractos `calculateArea()`
  y `calculatePerimeter()`, obligando a cada subclase a implementarlos
  (mismo contrato, distinta forma → polimorfismo).
- `Triangle` se define con `base` y `altura` (como pide el enunciado). Como
  con esos dos datos no alcanza para calcular el perímetro de un triángulo
  arbitrario, se asume un **triángulo isósceles** donde la altura cae en el
  punto medio de la base; los lados iguales se calculan con el teorema de
  Pitágoras. El supuesto está documentado en el Javadoc de la clase.
- Como ejercicio adicional de estudio personal (punto 4 de la guía, no
  evaluado), se puede crear una segunda versión usando una interfaz
  `Figure` en lugar de una clase abstracta.

## Cómo compilar y ejecutar

Requiere JDK 8+ y Maven (o usar el Maven embebido de IntelliJ IDEA).

```bash
mvn test        # compila y ejecuta las pruebas unitarias (JUnit 5)
mvn package      # genera el jar en target/
mvn exec:java -Dexec.mainClass=co.unicauca.polimorfismo.Main   # si se agrega exec-plugin
```

O simplemente abrir el proyecto en IntelliJ IDEA como proyecto Maven y ejecutar
`Main.java` / los tests desde el IDE.

## Autor

Edward Dávila — edwarddavilas36@gmail.com
