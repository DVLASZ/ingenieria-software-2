# Banco de preguntas Saber Pro con arquitectura hexagonal

Taller 12 del Laboratorio de Ingeniería de Software II (grupo 2). El dominio de una pregunta está en Java puro y se puede guardar con JPA y H2
o en memoria, sin cambiar el dominio. La API REST tiene tres endpoints:

- POST /api/preguntas crea una pregunta en BORRADOR
- POST /api/preguntas/{id}/enviar-revision la pasa a PENDIENTE_REVISION si tiene 4 opciones y una sola clave
- GET /api/preguntas/{id} la consulta

```
src/main/java/co/edu/unicauca/saberpro/
├── domain/          model, exception, ports.inbound, ports.outbound
├── application/     PreguntaService
└── infrastructure/  adapter.in.web, adapter.out.persistence (jpa y memory), adapter.out.notification, config
```

## Cómo correrlo

Necesita Java 17 o superior y Maven.

```bash
mvn test
mvn spring-boot:run
```

Con JPA es lo predeterminado. Para usar el adaptador en memoria se cambia `saberpro.persistencia` a `memoria` en
`src/main/resources/application.properties`, o se arranca así:

```bash
mvn spring-boot:run -Dspring-boot.run.arguments=--saberpro.persistencia=memoria
```

La consola de H2 está en http://localhost:8080/h2-console con la URL `jdbc:h2:mem:saberprodb` y el usuario `sa`, sin clave.
