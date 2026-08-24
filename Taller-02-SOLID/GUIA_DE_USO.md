# Guía de uso — Banco de Preguntas Saber Pro (módulo de usuarios)

Esta guía explica, paso a paso, cómo usar la aplicación desde que se abre
hasta que se entra al tablero. Está pensada para que cualquiera (incluido
el docente al momento de sustentarla) sepa exactamente qué escribir en
cada campo sin tener que adivinar ni leer el código.

## 1. Cómo abrir la aplicación

```bash
mvn package
java -jar target/taller02-solid.jar
```

o, desde IntelliJ IDEA, ejecutando la clase
`co.unicauca.saberpro.usuarios.app.MainApp`.

Al iniciar por primera vez se crea automáticamente un archivo
`saberpro.db` (SQLite) en la misma carpeta desde donde se ejecuta el jar.
**Ahí quedan guardados los usuarios entre una ejecución y otra** — si
cierras la aplicación y la vuelves a abrir, los usuarios que ya
registraste siguen existiendo. Si borras `saberpro.db`, la base queda
vacía otra vez.

## 2. Pantalla inicial: Inicio de sesión

Lo primero que aparece es la ventana **"Inicio de sesión"**, con dos
campos y dos botones:

| Campo | Qué escribir |
|---|---|
| Usuario | El *login* con el que te registraste (no el nombre completo) |
| Contraseña | La contraseña con la que te registraste |

- **Botón "Iniciar sesión"**: valida usuario y contraseña. Si son
  correctos y el usuario está **Activo**, abre el tablero correspondiente
  a su rol.
- **Botón "Registrarse"**: abre la ventana de registro (ver sección 3).

### La primera vez no tienes usuario todavía

Como la base de datos empieza vacía, **no hay ningún usuario creado por
defecto**. Lo primero que debes hacer siempre es pulsar **"Registrarse"**
y crear al menos un usuario antes de poder iniciar sesión.

### Mensajes de error al iniciar sesión

Si algo falla, aparece un cuadro de diálogo "No fue posible iniciar
sesión" con uno de estos mensajes:

- `Usuario o contraseña incorrectos` — el usuario no existe, o existe pero
  la contraseña no coincide (por seguridad no se distingue cuál de las dos
  cosas falló).
- `El usuario '...' se encuentra inactivo` — el usuario y la contraseña
  son correctos, pero fue registrado (o luego cambiado) con estado
  **Inactivo**. Debe registrarse con estado Activo, o cambiarse a Activo
  directamente en la base de datos, para poder entrar.

## 3. Ventana de registro

Se abre con el botón "Registrarse". Pide seis datos:

| Campo | Reglas |
|---|---|
| Usuario (login) | Obligatorio. No puede repetirse: si ya existe alguien con ese mismo usuario (sin importar mayúsculas/minúsculas), el registro falla. |
| Nombre completo | Obligatorio. |
| Rol | Uno de: **Administrador, Autor de preguntas, Revisor, Docente, Estudiante**. Se elige de una lista desplegable, no se escribe a mano. |
| Estado | **Activo** o **Inactivo**. Si registras el usuario como Inactivo, **no podrá iniciar sesión** hasta que alguien lo active. Para poder entrar de inmediato después de registrarte, deja **Activo**. |
| Contraseña | Ver reglas exactas abajo. |
| Confirmar contraseña | Debe ser idéntica a la contraseña. |

### Reglas exactas de la contraseña

La contraseña debe cumplir **las cuatro condiciones al mismo tiempo**:

1. Mínimo **6 caracteres** de longitud.
2. Al menos **un dígito** (0-9).
3. Al menos **una letra mayúscula** (A-Z).
4. Al menos **un carácter especial** (cualquier símbolo que no sea letra
   ni número: `! @ # $ % & * . , - _ ...`).

**Ejemplos:**

| Contraseña | ¿Válida? | Por qué |
|---|---|---|
| `Abcdef1$` | ✅ Sí | 8 caracteres, tiene mayúscula (A), dígito (1) y especial ($) |
| `abc12$` | ❌ No | le falta la mayúscula |
| `ABCDEF1` | ❌ No | le falta el carácter especial |
| `Abcdef$` | ❌ No | le falta el dígito |
| `Ab1$` | ❌ No | tiene menos de 6 caracteres |

Si la contraseña no cumple, el registro se rechaza y se muestra en
pantalla la lista exacta de reglas que faltan (por ejemplo: *"La
contraseña debe contener al menos un dígito"*). No hace falta adivinar:
el mensaje dice específicamente qué le falta.

La contraseña **nunca se guarda en texto plano**: se cifra con Argon2id
antes de escribirse en la base de datos, así que ni siquiera abriendo el
archivo `saberpro.db` se puede leer la contraseña original.

### Otros mensajes de error al registrar

- `Las contraseñas no coinciden` — lo escrito en "Contraseña" y en
  "Confirmar contraseña" no es igual.
- `El nombre de usuario es obligatorio` / `El nombre completo es
  obligatorio` — se dejó ese campo vacío.
- `El nombre de usuario '...' ya está registrado` — ese login ya existe;
  hay que elegir otro.

Si el registro es exitoso aparece "Usuario registrado correctamente" y la
ventana se cierra, volviendo a la pantalla de inicio de sesión para que
inicies sesión con el usuario recién creado.

## 4. El tablero (después de iniciar sesión)

Al iniciar sesión correctamente se abre una ventana de **Tablero** que
muestra:

- Un saludo con tu nombre completo y tu rol.
- Una lista de opciones de menú, **distinta según el rol** con el que
  iniciaste sesión (por ahora son solo opciones informativas de ejemplo;
  esas funcionalidades — banco de preguntas, simulacros, revisiones,
  etc. — se implementan en el proyecto de curso, no en este taller, que
  solo cubre la gestión de usuarios).

| Rol | Qué ve en el menú |
|---|---|
| Administrador | Gestionar usuarios, ver banco de preguntas, ver reportes, configurar el sistema |
| Autor de preguntas | Crear pregunta, editar mis borradores, enviar a revisión, consultar mis preguntas |
| Revisor | Ver preguntas asignadas, diligenciar evaluación, aprobar/rechazar, ver historial |
| Docente | Generar simulacro, ver reportes de estudiantes, ver estadísticas |
| Estudiante | Realizar simulacro, ver mi historial, ver mis estadísticas |

## 5. Flujo recomendado para probar la aplicación (por ejemplo, en la sustentación)

1. Abrir la aplicación (`java -jar target/taller02-solid.jar`).
2. Clic en **"Registrarse"**.
3. Llenar: usuario `admin1`, nombre completo `Ana Admin`, rol
   **Administrador**, estado **Activo**, contraseña `Admin123$` (cumple
   las 4 reglas), confirmar contraseña igual.
4. Clic en **"Registrar"** → debe salir "Usuario registrado
   correctamente".
5. En la pantalla de inicio de sesión, escribir usuario `admin1` y
   contraseña `Admin123$`, clic en **"Iniciar sesión"**.
6. Debe abrirse el tablero mostrando el menú de Administrador.
7. (Opcional) Repetir el registro con otro usuario y otro rol (por
   ejemplo `est1` / Estudiante) para comprobar que el menú cambia según
   el rol.
8. (Opcional) Intentar registrar dos veces el mismo usuario, o con una
   contraseña débil, para ver los mensajes de error descritos arriba.

## 6. Preguntas frecuentes

**¿Puedo ver la lista de usuarios registrados desde la interfaz?**
No en esta versión — el taller pide registro, autenticación y tablero por
rol; un listado administrable de usuarios se dejaría para una iteración
posterior del proyecto de curso. Sí existe internamente (`UserService.listUsers()`),
solo que no tiene pantalla propia todavía.

**¿Dónde quedan guardados los usuarios?**
En el archivo `saberpro.db` (SQLite), en la misma carpeta desde donde se
ejecutó el jar. Se puede abrir con cualquier cliente de SQLite (por
ejemplo la extensión de SQLite de VS Code, o el visor de IntelliJ IDEA)
para ver la tabla `Users` — la columna `PasswordHash` se ve como texto
cifrado ilegible, nunca la contraseña real.

**¿Qué pasa si borro `saberpro.db`?**
La próxima vez que abras la aplicación se crea una base nueva y vacía;
tendrás que volver a registrar los usuarios.
