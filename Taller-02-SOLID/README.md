# Taller 2 - Principios SOLID

Laboratorio de Ingeniería de Software II — Universidad del Cauca (Periodo 2-2026)

Módulo de **gestión de usuarios** (HU01 del proyecto de curso *"Sistema para
la Gestión, Validación y Administración de un Banco de Preguntas para la
Preparación de las Pruebas Saber Pro"*), implementado como aplicación de
escritorio Java (Swing) con persistencia en SQLite, aplicando los 5
principios SOLID **siguiendo la misma arquitectura del ejemplo 5 de
Inversión de Dependencias visto en la teoría** (`IProductRepository` /
`ProductRepository` / `Factory` / `Service` / `ClientMain`).

> **¿Primera vez usando la aplicación?** Ve directo a
> [`GUIA_DE_USO.md`](GUIA_DE_USO.md): explica paso a paso qué escribir en
> cada pantalla (login, registro, reglas exactas de la contraseña,
> mensajes de error) sin necesidad de leer el código.

## Funcionalidad

- Registro de usuarios: login, nombre completo, rol (Administrador, Autor de
  preguntas, Revisor, Docente, Estudiante), estado (Activo/Inactivo) y
  contraseña.
- Política de contraseña: mínimo 6 caracteres, al menos un dígito, una
  mayúscula y un carácter especial. Se almacena **cifrada con Argon2id**
  (nunca en texto plano).
- Inicio de sesión con usuario/contraseña.
- Tablero con opciones de menú **según el rol** del usuario autenticado.
- Persistencia en SQLite (archivo `saberpro.db`, se crea automáticamente).

## Cómo ejecutar

Requiere JDK 17+ (se probó con el JBR de IntelliJ IDEA) y Maven (o el Maven
embebido de IntelliJ).

```bash
mvn test        # ejecuta las 20 pruebas unitarias / de integración
mvn package      # genera target/taller02-solid.jar (con dependencias incluidas)
java -jar target/taller02-solid.jar
```

También se puede abrir el proyecto en IntelliJ IDEA como proyecto Maven y
ejecutar `co.unicauca.saberpro.usuarios.app.MainApp` directamente.

La base de datos SQLite (`saberpro.db`) se crea junto al jar/proyecto en el
primer arranque; no requiere instalación de un motor de base de datos aparte.

## Arquitectura y paquetes

El paquete `domain` refleja, para cada dependencia externa (persistencia,
cifrado, política de contraseñas), el mismo patrón **interfaz + implementación
concreta + fábrica singleton** del ejemplo visto en clase:

```
co.unicauca.saberpro.usuarios/
├── domain/
│   ├── User.java, Role.java, UserStatus.java      (entidades, sin dependencias externas)
│   ├── access/
│   │   ├── IUserRepository.java                    ≈ IProductRepository
│   │   ├── SqliteUserRepository.java                ≈ ProductRepository (JDBC, maneja su propia conexion)
│   │   └── UserRepositoryFactory.java               ≈ Factory (singleton, getRepository("default"))
│   ├── security/
│   │   ├── IPasswordHasher.java
│   │   ├── Argon2PasswordHasher.java
│   │   └── PasswordHasherFactory.java               (mismo patron: singleton + getHasher("default"))
│   ├── validation/
│   │   ├── IPasswordPolicy.java
│   │   ├── DefaultPasswordPolicy.java
│   │   └── PasswordPolicyFactory.java               (mismo patron: singleton + getPolicy("default"))
│   ├── service/
│   │   ├── UserService.java                         ≈ Service (clase concreta, recibe las 3 interfaces por constructor)
│   │   └── exception/                                (DuplicateUsernameException, InvalidPasswordException, AuthenticationException)
│   └── menu/
│       ├── IMenuProvider.java + 5 implementaciones por rol
│       └── MenuProviderRegistry.java                 (resuelve el IMenuProvider segun el rol)
├── ui/                                                (LoginFrame, RegisterFrame, DashboardFrame - Swing)
└── app/
    └── MainApp.java                                   ≈ ClientMain (composition root: usa las 3 fabricas)
```

## Aplicación de los principios SOLID

**S — Single Responsibility.**
Cada clase tiene una única razón para cambiar: `User` solo modela datos,
`Argon2PasswordHasher` solo cifra/verifica, `DefaultPasswordPolicy` solo
valida reglas de contraseña, `SqliteUserRepository` solo persiste,
`UserService` solo orquesta el caso de uso, y cada `*Frame` solo se encarga
de su pantalla.

**O — Open/Closed.**
- `IPasswordHasher` e `IPasswordPolicy` se pueden extender (nuevo algoritmo,
  nueva regla) creando una clase nueva y agregando un `case` en su fábrica,
  sin tocar `UserService`.
- `IMenuProvider` permite agregar un nuevo rol con su propio menú (nueva
  clase) sin modificar `DashboardFrame` ni `MenuProviderRegistry`.
- `IUserRepository` permite cambiar de SQLite a otro motor agregando una
  nueva implementación y un nuevo `case` en `UserRepositoryFactory`.

**L — Liskov Substitution.**
Cualquier implementación de `IUserRepository`, `IPasswordHasher` o
`IMenuProvider` puede sustituir a otra sin romper a quien la usa: todas
respetan el mismo contrato. Por ejemplo, en las pruebas se reemplaza
`SqliteUserRepository` por un mock de `IUserRepository` sin tocar
`UserService`.

**I — Interface Segregation.**
En vez de una interfaz "gorda" con todo mezclado, hay tres interfaces
pequeñas y cohesivas: `IUserRepository` (persistencia), `IPasswordHasher`
(cifrado) y `IPasswordPolicy` (reglas de negocio). Ninguna implementación
se ve forzada a depender de métodos que no usa.

**D — Dependency Inversion.**
`UserService` (módulo de alto nivel, igual que `Service` en el ejemplo de
clase) depende únicamente de las interfaces `IUserRepository`,
`IPasswordHasher` e `IPasswordPolicy` — nunca de `SqliteUserRepository` ni
de `Argon2PasswordHasher` directamente. Las implementaciones concretas se
obtienen a través de fábricas singleton (`UserRepositoryFactory`,
`PasswordHasherFactory`, `PasswordPolicyFactory`) y se inyectan por
constructor. Todo el cableado ocurre en un único lugar, `MainApp`
(composition root), replicando exactamente cómo `ClientMain` usa `Factory`
en el ejemplo 5 de Inversión de Dependencias visto en la clase teórica.

## Pruebas (20 en total)

- `DefaultPasswordPolicyTest` (6): reglas de la política de contraseñas.
- `UserServiceTest` (7): casos de uso de registro/autenticación con
  `IUserRepository`, `IPasswordHasher` e `IPasswordPolicy` **mockeados**
  (Mockito) — no requieren base de datos real.
- `SqliteUserRepositoryTest` (4): prueba de integración contra una base de
  datos SQLite en memoria.
- `Argon2PasswordHasherTest` (3): verifica que el hash difiere de la
  contraseña en texto plano y que `matches` funciona correctamente.

```bash
mvn test
```

## Autores

Taller realizado en pareja, según lo permite la guía:

- Edward Dávila — edwarddavila@unicauca.edu.co
- Laura Isabel Sánchez Fernández
