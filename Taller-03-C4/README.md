# Taller de Laboratorio — Modelo C4

Laboratorio de Ingeniería de Software II — Universidad del Cauca (Periodo 2-2026)

Diseño de la arquitectura de software del **Sistema de Banco de Preguntas
Saber Pro** (proyecto de curso) usando el **modelo C4**, aplicando
principios de diseño orientado a objetos (SOLID) al pasar de los
requisitos funcionales/no funcionales a una representación arquitectónica
en capas: **Presentación, Dominio, Acceso a Datos y Transversal**.

Este taller se resolvió a partir de dos guías: la guía general del taller
(Actividades 1 a 5) y la **guía paso a paso para diagrams.net** entregada
después, que exige explícitamente construir los 4 niveles con la
**biblioteca oficial C4** del editor (no figuras genéricas) y dedica el
Nivel 4 al componente de **Gestión de Preguntas**.

## Contenido

| Archivo | Qué contiene |
|---|---|
| [`INFORME.md`](INFORME.md) | **Informe completo del taller**: actores, funcionalidades, historias de usuario, los 4 niveles del modelo C4 (con las imágenes exportadas) y las decisiones de arquitectura tomadas |
| [`diagramas/03-TallerC4-SaberPro.drawio`](diagramas/03-TallerC4-SaberPro.drawio) | Archivo fuente editable, los 4 niveles, uno por pestaña, construidos con la biblioteca C4/UML oficial |
| [`diagramas/imagenes/`](diagramas/imagenes) | Los 4 niveles exportados a PNG (los mismos que aparecen en `INFORME.md`) |

Toda la información del taller (Actividad 1 a 5) está consolidada en
[`INFORME.md`](INFORME.md); ya no está repartida en varios archivos
`.md` sueltos.

## Cómo abrir y editar el diagrama fuente

1. Entrar a [app.diagrams.net](https://app.diagrams.net/).
2. **Archivo → Abrir desde → Dispositivo**.
3. Seleccionar `diagramas/03-TallerC4-SaberPro.drawio`.
4. Las 4 pestañas (Nivel 1 a Nivel 4) aparecen en la barra inferior.

Las imágenes en `diagramas/imagenes/` se exportaron desde ahí con
**Archivo → Exportar como → PNG…**, zoom 150-200 %, fondo blanco (sin
transparencia), "página actual".

## Decisiones de arquitectura relevantes

- **Biblioteca C4 real, no rectángulos coloreados:** las figuras de
  *Person*, *Software System*, *Container*, *Component* y las relaciones
  usan exactamente los estilos (`shape=mxgraph.c4.person2`, colores
  oficiales, etc.) de `+ Más formas → Software → C4` en diagrams.net —
  se verificaron abriendo el editor, activando esa biblioteca y copiando
  el estilo real de cada figura antes de generar el archivo.
- **Un solo archivo, 4 páginas:** igual que indica la guía paso a paso
  (crear una pestaña por nivel dentro del mismo documento), no 4 archivos
  sueltos.
- **Requerimiento arquitectónico (monolito en capas):** las 4 capas
  exigidas por la guía se modelan a nivel de **Componentes** (Nivel 3),
  no de Contenedores (Nivel 2) — un contenedor en C4 es una unidad
  desplegable/tecnológica separada, y aquí el monolito completo es un
  único contenedor (más la base de datos como segundo contenedor). Las
  capas son la forma en que ese contenedor se organiza internamente.
- **Nivel 4 = Gestión de Preguntas, no Gestión de usuarios:** aunque la
  gestión de usuarios ya está implementada y probada en
  [`Taller-02-SOLID/`](../Taller-02-SOLID), la guía paso a paso pide
  explícitamente el componente de preguntas para el Nivel 4 (con la
  jerarquía `Pregunta` → `PreguntaDirecta` / `PreguntaSeleccionMultiple`
  para OCP/LSP). Ver el detalle y la justificación en
  [`INFORME.md`](INFORME.md).
- **Nivel 3 mapea las 13 funcionalidades mínimas de la Actividad 1, una
  por una:** 6 controladores + 9 componentes de dominio (5 con
  controlador propio, 4 colaboradores internos: `ValidadorPregunta`,
  `CicloVidaPreguntaService`, `CalificacionService`, `SeguimientoService`)
  + 4 repositorios + 3 componentes transversales — en vez de agrupar
  varias HU en un solo componente genérico.
- **Nivel 4 cierra referencias sueltas y añade el ciclo de vida:** se
  dibujó `ValidadorPregunta` (antes solo mencionado como atributo) y el
  enum `EstadoPregunta` (RF-14) como parte de `Pregunta`, conectando este
  nivel con `CicloVidaPreguntaService` del Nivel 3.
- **Verificación automática de sobreposiciones:** los 4 niveles se
  validaron con un script que confirma 0 cajas superpuestas antes de
  exportar las imágenes finales.

## Autores

Taller realizado en pareja:

- Edward Dávila — edwarddavilas36@gmail.com
- Laura Isabel Sánchez Fernández
