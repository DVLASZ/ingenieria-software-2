# Preguntas para la profesora (Taller 09)

> **Respondidas.** Los nueve servicios van completos; los estados y el rechazo son los del proyecto; el token es
> simple por ahora; el código se guarda en el repositorio de laboratorio. Lo demás se asumió con las propuestas de
> este documento. El resto del texto se conserva como registro de lo que se preguntó.

Mensaje para pegar en clase o en el Classroom. Cada pregunta trae la propuesta del grupo para que solo tenga
que confirmar o corregir. Todo se explica en
[`DISENO-MICROSERVICIOS.md`](DISENO-MICROSERVICIOS.md).

---

Buenas tardes, profesora. Somos el grupo 2 (Edward y Laura). Vamos a hacer el taller como una **versión lite
de nuestro proyecto del banco de preguntas**: los mismos estados, las mismas reglas de revisión y la misma
forma de rechazar. Usaremos Spring Web, `RabbitTemplate`, `@RabbitListener`, puertos distintos y variables de
configuración por servicio, tal como lo pidió. Estas son nuestras dudas:

**1. Más microservicios que los seis sugeridos.** Además de los seis, proponemos tres más a partir de los
contextos que definimos para el proyecto: `history-service` (Administración y Trazabilidad, consume todos los
eventos), `simulation-service` (Simulacros, se alimenta de las preguntas publicadas) y `tracking-service`
(Seguimiento Académico, consume los resultados de los simulacros). ¿Le parece adecuado? ¿Espera que todos estén
implementados con sus endpoints y mensajes, o basta con una primera muestra (diseño completo y lo mínimo
funcionando) de los que no son los seis del taller?

**2. Validación y Ciclo de Vida como servicios.** En nuestro proyecto son módulos internos de
`question-service`; aquí los separamos como pide el taller. ¿Está bien que expliquemos esa diferencia en el
informe?

**3. Nombres de los estados.** Usaremos los siete estados del proyecto: Borrador, Pendiente de revisión, En
revisión, Aprobada, Rechazada, Publicada y Archivada (en código `BORRADOR`, `PENDIENTE_REVISION`, etc.), con las
mismas transiciones. ¿Acepta que la API los devuelva con esos nombres, junto con su equivalente en inglés
(`DRAFT`, `PENDING_REVIEW`, `IN_REVIEW`, `APPROVED`, `REJECTED`, `PUBLISHED`, `ARCHIVED`), o los nombres del
enunciado en inglés son obligatorios?

**4. Forma de rechazar.** Como en el proyecto: el administrador asigna de 1 a 3 revisores, la pregunta se aprueba
solo si **todos** aprueban, y **un solo rechazo** deja la pregunta en Rechazada con las observaciones (rechazar
exige observación). El autor la corrige (vuelve a Borrador), se valida otra vez y se reenvía, lo que abre una
nueva ronda. ¿Le parece correcto para el escenario del taller?

**5. Dónde van aprobar y rechazar.** El enunciado no dice qué servicio los expone. Proponemos que sea
`review-service` (`POST /reviews/{questionId}/evaluations` con la decisión y la observación), que pide la
transición a `lifecycle-service` por REST, igual que la asignación. Publicar y archivar los pide `question-service`
con un comando por RabbitMQ y respuesta 202. ¿Está bien?

**6. Token en `Authorization`.** El paso 1 dice que no se implementan tokens ni JWT, pero el paso 2 pide que
`POST /questions` lleve el token en el encabezado `Authorization`. Proponemos que el login devuelva un valor
simple y que Question Service exija que el encabezado exista y no esté vacío, sin validarlo contra el User
Service (el JWT con Keycloak es del corte 3). ¿Qué espera usted exactamente?

**7. Roles.** En el proyecto los roles son Autor, Revisor y Administrador, sin roles de docente ni estudiante.
El enunciado habla del «docente» que crea y envía preguntas. ¿Podemos usar nuestros roles y llamar «autor» al
docente?

**8. Falla de una transición asíncrona.** Para mostrarla provocaremos una transición inválida (por ejemplo,
enviar a revisión una pregunta que ya no está en Borrador): el resultado de la operación queda en `FAILED` con
el motivo y el cliente lo consulta en `GET /operations/{operationId}`. ¿Le sirve así?

**9. Consistencia en la asignación.** Pedimos primero la transición a En revisión a `lifecycle-service` y
registramos la asignación solo si tuvo éxito; si falla, no se registra nada y se responde con error. ¿Es
suficiente o espera una compensación?

**10. Reglas del Validation Service.** Aplicaremos las de nuestro proyecto: contexto presente, una única pregunta
directa, cuatro distractores, una única respuesta correcta y sin «todas/ninguna de las anteriores». ¿Basta con
eso?

**11. Cómo debe aparecer en el código.** Para cumplir lo que pidió, cada servicio tendrá su propio
`RabbitMQConfig`, clases `*Publisher` con `RabbitTemplate.convertAndSend(...)`, clases `*Listener` con
`@RabbitListener`, y su propio puerto y variables de entorno, sin una biblioteca común entre servicios. ¿Es lo
que espera?

**12. Persistencia.** ¿Vale H2 en memoria por servicio (una base por servicio, sin compartir tablas), o los datos
deben sobrevivir al reiniciar?

**13. RabbitMQ.** ¿Es obligatorio Docker o aceptan RabbitMQ instalado en el equipo? Para las capturas del panel
(`localhost:15672`), ¿qué quiere ver: exchanges, colas, mensajes publicados y consumidos?

**14. Repositorio.** ¿Debe ser un repositorio nuevo solo para el taller (con las carpetas de los servicios en la
raíz) o sirve una carpeta dentro de nuestro repositorio de laboratorio? ¿Debe ser público?

**15. Cliente y sustentación.** El paso 1 habla de una «aplicación de escritorio», pero las pruebas son en
Postman. ¿Basta con Postman? Y para la sustentación, ¿qué debemos demostrar en vivo?

Muchas gracias.

---

## Las más importantes si hay poco tiempo

1 (más microservicios), 3 (nombres de los estados), 4 (rechazo), 6 (token) y 14 (repositorio).
