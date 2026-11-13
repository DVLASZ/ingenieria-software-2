# Taller 12: arquitectura hexagonal y DDD

Taller del Laboratorio de Ingeniería de Software II (noviembre de 2026), en pareja: Edward Esteban Dávila Salazar y Laura Isabel
Sánchez Fernández (grupo 2). Es la primera parte de un taller dividido en dos. Lleva el dominio del banco de preguntas Saber Pro a una
arquitectura hexagonal y comprueba que el repositorio JPA se puede cambiar por uno en memoria sin tocar el dominio. La segunda parte es la
presentación del trabajo final.

| Carpeta o archivo | Contenido |
|---|---|
| [`lisw2_taller12_hexagonal_g2/`](lisw2_taller12_hexagonal_g2/) | El proyecto Spring Boot con el dominio, los puertos, los dos adaptadores y las pruebas |
| [`INFORME.md`](INFORME.md) | El informe: modelo de dominio, puertos y adaptadores, prueba de intercambiabilidad y pruebas |
| [`lisw2_taller12_g2.docx`](lisw2_taller12_g2.docx) | El mismo informe en la plantilla oficial |
| [`img/`](img/) | Los diagramas del informe |

Para correrlo: `mvn test` dentro de `lisw2_taller12_hexagonal_g2`, con Java 17 o superior.
