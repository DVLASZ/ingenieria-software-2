# Taller 6 — API REST con Spring Boot

Laboratorio de Ingeniería de Software II — Universidad del Cauca (Periodo 2-2026)

Igual que en el Taller 5, el código de este taller **no se duplica aquí**:
vive en el repositorio del proyecto de curso,
[`banco-preguntas-saberpro`](https://github.com/DVLASZ/banco-preguntas-saberpro),
dentro del módulo
[`modulo-api-rest`](https://github.com/DVLASZ/banco-preguntas-saberpro/tree/main/modulo-api-rest).
Esta carpeta solo deja constancia de qué se hizo y de dónde revisarlo.

## Qué se implementó

Un microservicio REST con **Spring Boot 3.5** y **Spring Data JPA** que expone
el CRUD de las preguntas del banco (la entidad `Question` real del proyecto,
no una de ejemplo aparte) en `/api/questions`:

| Verbo | Ruta | Resultado |
|---|---|---|
| GET | `/api/questions` | Lista las preguntas |
| GET | `/api/questions/{id}` | Consulta una pregunta (404 si no existe) |
| POST | `/api/questions` | Crea una pregunta (201 + `Location`) |
| PUT | `/api/questions/{id}` | Actualiza su contenido |
| DELETE | `/api/questions/{id}` | La archiva (204): no se borra físicamente por RNF-16 |

- Arquitectura en capas `controller → service → repository → model`, como
  pide la guía del taller, con la persistencia hecha con **JPA sobre H2**
  como pide el enunciado.
- El dominio no depende de JPA: `QuestionEntity` está separada de `Question`
  y un adaptador implementa el puerto `QuestionRepository`.
- Manejo de errores uniforme (400 con el detalle de cada campo, 404, 500).
- 43 pruebas automatizadas y una colección de Postman con las 8 peticiones.

## Dónde revisarlo

- Código, pruebas y colección de Postman: [`banco-preguntas-saberpro/modulo-api-rest`](https://github.com/DVLASZ/banco-preguntas-saberpro/tree/main/modulo-api-rest)
- Arquitectura, endpoints y cómo ejecutarlo: el `README.md` de ese módulo.

## Autores

Taller realizado en pareja:

- Edward Dávila — edwarddavila@unicauca.edu.co
- Laura Isabel Sánchez Fernández
