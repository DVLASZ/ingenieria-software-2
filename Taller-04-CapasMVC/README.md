# Taller 4 — Patrón en Capas y Micropatrón MVC

Laboratorio de Ingeniería de Software II — Universidad del Cauca (Periodo 2-2026)

Módulo de **Gestión de Preguntas** del proyecto de curso (Sistema de Banco
de Preguntas Saber Pro), implementado como aplicación Java SE de
escritorio (Swing), aplicando **arquitectura en capas**, el **micropatrón
MVC** y el **patrón Observer** para mantener sincronizadas varias vistas
ante un mismo cambio de estado.

**Este taller ya integra también el [Taller 2](../Taller-02-SOLID)**
(login, usuarios y roles): la aplicación arranca con la pantalla de
inicio de sesión y, según el rol autenticado, abre la ventana
correspondiente de este taller — es el primer paso de ir fusionando los
talleres sueltos hacia el proyecto de curso completo. Ver la sección
[Fusión con el Taller 2](#fusión-con-el-taller-2-login-y-roles) más abajo.

El ciclo de vida de una pregunta (`EstadoPregunta`) usa exactamente los 7
estados definidos en **RF-14** del proyecto de curso (ver
[`Taller-03-C4/INFORME.md`](../Taller-03-C4/INFORME.md)): `Borrador`,
`Pendiente de revisión`, `En revisión`, `Aprobada`, `Rechazada`,
`Publicada` y `Archivada` — no una simplificación propia — para que este
taller sea congruente con el resto del proyecto.

## Objetivo del taller

Implementar una solución al proyecto del Banco de Preguntas utilizando
una arquitectura en capas, el micropatrón MVC y el patrón Observer: al
cambiar el estado de una pregunta, dos vistas independientes
(estadísticas y gráfica de pastel) deben notificarse y refrescarse
automáticamente, sin que la ventana que originó el cambio conozca sus
detalles internos.

## Fusión con el Taller 2 (login y roles)

Siguiendo la distribución de actores del modelo C4 del
[Taller 3](../Taller-03-C4/INFORME.md), la aplicación ya no abre todas
las ventanas de una vez: arranca con `LoginFrame` (traído del
[Taller 2](../Taller-02-SOLID), paquete `usuarios`) y, según el
`Role` del usuario autenticado, abre solo la ventana que le corresponde:

| Rol autenticado | Ventana que se abre |
|---|---|
| `AUTOR_PREGUNTAS` | `GUIQuestions` (redactar/crear preguntas) |
| `REVISOR` | `GUIRevisor` (evaluar y decidir) |
| `DOCENTE` | `GUIDocente` (generar simulacros, HU-12) |
| `ESTUDIANTE` | `GUIEstudiante` (presentar simulacros, HU-13/HU-14) |
| `ADMINISTRADOR` | `DashboardFrame` genérico del Taller 2 (su vista real aún no existe en este taller — queda para una siguiente iteración) |

Esta decisión vive en `SesionRouter` (interfaz en `usuarios.ui`,
implementada como lambda en el composition root `app.MainApp`): así
`LoginFrame` nunca depende de las ventanas concretas del módulo de
preguntas, solo de esa abstracción (Inversión de Dependencias, igual que
en el resto del Taller 2).

**Usuarios de prueba** (se crean automáticamente la primera vez que se
ejecuta la app, si `saberpro.db` está vacía): `autor1`, `revisor1`,
`docente1`, `estudiante1`, `admin1` — contraseña `Saber2026!` para todos.

### Autor de preguntas y Revisor no son la misma pantalla

El **Autor** y el **Revisor** son responsabilidades distintas y por eso
tienen ventanas separadas —no un único formulario que hace ambas cosas—:

- **`GUIQuestions`** (rol **Autor de preguntas**): redacta el contenido
  de una pregunta — **crea preguntas nuevas y edita las existentes**
  (nombre, enunciado, opciones, respuesta correcta) (HU-04). Al crear una
  pregunta esta queda automáticamente en `PENDIENTE_REVISION`: el Autor
  la redacta y la envía, pero no tiene ningún control para decidir su
  aprobación ni cambiar su estado por ningún otro medio.
- **`GUIRevisor`** (rol **Revisor**): muestra la pregunta en solo
  lectura (nunca edita el contenido) y es la **única** ventana con la
  potestad de decidir su resultado — botones **Aprobar** / **Rechazar**
  (RF-16). Al cargar una pregunta que está `PENDIENTE_REVISION`, el solo
  hecho de abrirla la pasa automáticamente a `EN_REVISION` (RF-15): el
  Revisor "la toma" para evaluarla antes de decidir.

Esta separación es la misma que ya tenían los mockups de Figma del
taller: el Autor gestiona contenido, el Revisor gestiona el ciclo de
vida.

### Docente y Estudiante: generar y presentar simulacros

Siguiendo HU-12 a HU-14 del proyecto de curso, cada pregunta ahora
también se clasifica por **competencia**, **tema** y **dificultad**
(`Competencia`, `Dificultad`), lo que permite un nuevo módulo de
**simulacros** (`preguntas.simulacro`, con su propio dominio/acceso, en
memoria igual que `preguntas`):

- **`GUIDocente`** (rol **Docente**, HU-12/RF-21/RF-22): genera un
  simulacro para un grupo, filtrando por competencia, tema y/o
  dificultad — solo puede usar preguntas ya `PUBLICADA`s. Si ningún
  filtro coincide con ninguna pregunta publicada, se lo informa en vez
  de generar un simulacro vacío. Los simulacros generados quedan
  listados en la misma ventana.
- **`GUIEstudiante`** (rol **Estudiante**, HU-13/HU-14/RF-23/RF-24):
  elige uno de los simulacros disponibles y lo presenta dentro de su
  tiempo máximo (cronómetro real con `javax.swing.Timer`, que finaliza el
  intento automáticamente si llega a cero); navega entre preguntas con
  "Anterior"/"Siguiente" sin perder las respuestas ya marcadas, y al
  "Finalizar" el sistema **califica automáticamente** comparando cada
  respuesta contra `Question.getRespuestaCorrecta()` (las preguntas sin
  responder cuentan como incorrectas) y muestra el resultado.

`SimulacroService` depende de `QuestionService` (reutiliza
`buscarPublicadas`) y de la abstracción `SimulacroRepository` — nunca de
`QuestionImplRepository` ni de una tecnología de persistencia concreta
(DIP, igual que el resto de la app). Las preguntas de un simulacro
quedan "congeladas" en el momento de generarlo: si después cambian de
estado o contenido, los simulacros ya generados no se ven afectados.

No forman parte de este alcance (quedan para una siguiente iteración
del proyecto de curso): el historial de simulacros de un estudiante
(HU-15) y los reportes/estadísticas agregadas (HU-16, HU-17) — el
dominio ya guarda lo necesario (`SimulacroRepository.listarIntentosDeEstudiante`)
pero todavía no tienen una ventana propia.

## Arquitectura en capas

Dos módulos conviven en el mismo proyecto Maven, cada uno con sus propias
capas, y se comunican únicamente a través de `SesionRouter`:

**Módulo `preguntas` (Taller 4 — capas + MVC + Observer):**

| Capa | Paquete | Responsabilidad |
|---|---|---|
| **Presentación** | `preguntas.presentation` | `GUIQuestions` (rol Autor: crear/editar preguntas), `GUIRevisor` (rol Revisor: aprobar/rechazar), `GUIDocente` (rol Docente: generar simulacros), `GUIEstudiante` (rol Estudiante: presentar simulacros), `GUIObserver1` (vista de estadísticas), `GUIObserver2` (vista gráfica de pastel) |
| **Dominio** | `preguntas.domain` | `Question`, `QuestionDistractors`, `EstadoPregunta`, `Competencia`, `Dificultad`, `QuestionRepository` (contrato) y `QuestionService` (lógica de negocio + `Subject` concreto) |
| **Acceso a datos** | `preguntas.access` | `QuestionImplRepository`: implementación en memoria (sin base de datos relacional, tal como permite la guía) |
| **Transversal** | `preguntas.infra` | `Observer` y `Subject`: interfaces genéricas y reutilizables del patrón Observer (GoF), sin conocimiento del dominio de preguntas |

**Submódulo `preguntas.simulacro` (HU-12 a HU-14 — Docente/Estudiante):**

| Capa | Paquete | Responsabilidad |
|---|---|---|
| **Dominio** | `preguntas.simulacro.domain` | `Simulacro`, `IntentoSimulacro`, `EstadoIntento`, `SimulacroRepository` (contrato) y `SimulacroService` (generación filtrada, presentación y calificación automática) |
| **Acceso a datos** | `preguntas.simulacro.access` | `SimulacroImplRepository`: implementación en memoria, mismo criterio que `QuestionImplRepository` |

**Módulo `usuarios` (Taller 2 — SOLID, traído tal cual):**

| Capa | Paquete | Responsabilidad |
|---|---|---|
| **Presentación** | `usuarios.ui` | `LoginFrame`, `RegisterFrame`, `DashboardFrame` (fallback genérico), `SesionRouter` (abstracción que decide la vista según el rol) |
| **Dominio** | `usuarios.domain` | `User`, `Role`, `UserStatus`, `service.UserService` (registro/autenticación), `menu.*` (Strategy de opciones por rol) |
| **Acceso a datos** | `usuarios.domain.access` | `SqliteUserRepository`: persistencia real en `saberpro.db` |
| **Transversal** | `usuarios.domain.security`, `usuarios.domain.validation` | `Argon2PasswordHasher`, `DefaultPasswordPolicy` |

Todas las dependencias apuntan hacia el dominio: `access` implementa la
interfaz `QuestionRepository`/`IUserRepository` definida en `domain`
(Inversión de Dependencias), y `presentation`/`ui` solo conocen sus
respectivos servicios y abstracciones. El único punto donde ambos
módulos se conocen es el composition root, `app.MainApp`.

## MVC + Observer: cómo se sincronizan las vistas

`QuestionService` es el **Subject concreto**: `GUIObserver1` y
`GUIObserver2` se suscriben a él (`agregarObservador`) al arrancar la
aplicación. Se notifica en **tres** momentos, no solo al decidir:

```
GUIQuestions.guardar()                              (Autor crea/edita)
    -> QuestionService.crearPregunta(...) | actualizarContenido(...)
        -> QuestionRepository.crear(pregunta) | actualizar(pregunta)
        -> Subject.notificarObservadores()

GUIRevisor.cargarPreguntaSeleccionada()              (Revisor "toma" la pregunta)
    -> QuestionService.cambiarEstado(id, EN_REVISION)   [solo si estaba PENDIENTE_REVISION]
        -> Subject.notificarObservadores()

GUIRevisor.decidir(APROBADA | RECHAZADA)             (Revisor decide)
    -> QuestionService.cambiarEstado(id, nuevoEstado)
        -> Question.setEstado(nuevoEstado)
        -> QuestionRepository.actualizar(pregunta)
        -> Subject.notificarObservadores()
            -> GUIObserver1.actualizar()  (recalcula conteo por estado)
            -> GUIObserver2.actualizar()  (recalcula porcentajes y redibuja el pastel)
```

Ni `GUIObserver1` ni `GUIObserver2` conocen a `GUIRevisor`, y
`QuestionService` no conoce las clases concretas de sus observadores
(solo la interfaz `Observer`) — el acoplamiento es mínimo, tal como exige
el patrón.

## Interfaz gráfica

Sigue el prototipo entregado en la guía (`Actividad 1` / diagrama
PlantUML del anexo), con un look and feel moderno
([FlatLaf](https://www.formdev.com/flatlaf/), tema claro con acento
azul) en vez del Swing/Metal por defecto, y una paleta de color
semántica por estado (definida una sola vez en `EstadoColores` y
reutilizada por las 4 ventanas) para que el mismo estado se reconozca
por color en toda la aplicación:

- **Autor de preguntas** (`GUIQuestions`): `ComboBox` para elegir una
  pregunta existente ("Cargar pregunta") o empezar una nueva ("Nueva
  pregunta"), con Nombre, Pregunta, Opciones A-D y Respuesta correcta
  **editables**, botón "Guardar" y el Estado actual como insignia de
  color — de solo lectura (el Autor nunca lo cambia manualmente).
- **Revisor** (`GUIRevisor`): la información de la pregunta en solo
  lectura, más los botones "Aprobar" y "Rechazar" — la única forma de
  decidir el resultado de una pregunta en la aplicación.
- **Vista de estadísticas**: cantidad de preguntas en cada uno de los 7
  estados de RF-14, con un indicador de color por fila y el total.
- **Vista gráfica**: diagrama de pastel con el porcentaje de preguntas en
  cada estado (dibujado con `Graphics2D.fillArc`, sin librerías externas
  de gráficos) y una leyenda con muestra de color junto a cada estado.
- **Docente** (`GUIDocente`): filtros de Competencia/Tema/Dificultad,
  Grupo, cantidad de preguntas y tiempo máximo, botón "Generar
  simulacro" y la lista de simulacros ya generados.
- **Estudiante** (`GUIEstudiante`): selector de simulacro disponible y,
  al presentarlo, progreso ("Pregunta X de N"), cronómetro regresivo,
  las 4 opciones como radio buttons y los botones
  "Anterior"/"Siguiente"/"Finalizar".

## Cómo compilar y ejecutar

```bash
mvn test      # ejecuta las 73 pruebas unitarias/integración
mvn package   # genera target/taller04-capas-mvc.jar
java -jar target/taller04-capas-mvc.jar
```

Al iniciar:
- se cargan 12 preguntas de ejemplo (repositorio en memoria) con
  distintos estados, competencias, temas y dificultades, para poder ver
  de inmediato las estadísticas y la gráfica con datos no triviales, y
  para que el Docente tenga con qué generar un simulacro (5 de esas 12
  ya están `PUBLICADA`s, repartidas entre 4 competencias distintas);
- se crea `saberpro.db` (SQLite, en la carpeta desde donde se ejecuta) y,
  si está vacía, se siembran 5 usuarios de prueba (uno por rol) — ver la
  tabla de credenciales en [Fusión con el Taller 2](#fusión-con-el-taller-2-login-y-roles);
- se muestra primero `LoginFrame`; tras autenticar, se abre la ventana
  correspondiente al rol.

## Pruebas unitarias

73 pruebas (JUnit 5 + Mockito) en 11 clases — 53 del módulo `preguntas`
(Taller 4, incluyendo el submódulo `simulacro`) + 20 del módulo
`usuarios` (Taller 2, sin modificar):

| Clase de prueba | Qué prueba |
|---|---|
| `QuestionTest` | Validaciones del constructor (incluye competencia/tema/dificultad obligatorios) y de `setEstado` de la entidad `Question` |
| `QuestionServiceTest` | Lógica de negocio: cambio de estado, creación (queda `PENDIENTE_REVISION`), edición de contenido (conserva el estado), conteo por estado, `buscarPublicadas` (solo `PUBLICADA` + filtros de competencia/tema/dificultad) y — con un `Observer` mockeado — que `notificarObservadores()` sí invoca a cada observador suscrito (y deja de hacerlo tras `eliminarObservador`) |
| `QuestionImplRepositoryTest` | CRUD del repositorio en memoria: datos de ejemplo, búsqueda por id, actualización, creación con id nuevo/duplicado, generación de ids consecutivos |
| `SimulacroTest` | Validaciones del constructor de `Simulacro` (preguntas, grupo, tiempo máximo) e inmutabilidad de su lista de preguntas |
| `IntentoSimulacroTest` | Ciclo de vida de un intento: responder, rechazar respuestas tras finalizar, y que `finalizar` solo pueda ocurrir una vez |
| `SimulacroServiceTest` | Generación filtrada (usa `QuestionService.buscarPublicadas` mockeado), tope a la cantidad pedida, excepción si ningún filtro coincide, y calificación automática al finalizar (con `QuestionService`/`SimulacroRepository` mockeados) |
| `SimulacroImplRepositoryTest` | CRUD en memoria de simulacros e intentos, generación de ids consecutivos, filtro de intentos por estudiante |
| `DefaultPasswordPolicyTest` | Reglas de complejidad de contraseñas |
| `Argon2PasswordHasherTest` | Hash y verificación de contraseñas |
| `SqliteUserRepositoryTest` | CRUD de usuarios contra SQLite en memoria |
| `UserServiceTest` | Registro y autenticación, con los 3 colaboradores mockeados (DIP) |

## Autores

Taller realizado en pareja:

- Edward Dávila — edwarddavila@unicauca.edu.co
- Laura Isabel Sánchez Fernández
