# Documentacion - Sistema de Administracion de Torneos Deportivos

## 1. Control de Versiones

### Configuracion Git y GitHub

El proyecto utiliza Git para control de versiones con la siguiente estructura de ramas:

- **master**: rama principal con versiones estables
- **development**: rama de desarrollo para integracion de features
- **feature/...**: ramas para desarrollo de nuevas funcionalidades

### Commits

Los commits se realizan de forma convencional:
- El tipo de cambio (feat, fix, refactor, chore, docs)
- Descripcion del cambio realizado

## 2. Autenticacion y Seguridad

### Implementacion

#### Login basado en BD
- Tabla `usuarios` almacena credenciales
- Tabla `auth_tokens` persiste tokens de sesion
- Sistema basado en JWT tokens

#### Encriptacion de Contraseña
Implementacion: `Sha1PasswordEncoder` en `security/`

Clase: `dev.mrraxyer.sportstournamentmanager.security.Sha1PasswordEncoder`

Algoritmo: SHA-1

Proceso:
1. Usuario envia credenciales en login
2. Contraseña se encripta con SHA-1
3. Se compara con valor almacenado en BD
4. Si coincide, se genera JWT token
5. Token se persiste en `auth_tokens` con expiracion

#### Endpoints de Autenticacion
- POST `/auth/login` - Autenticar usuario
- POST `/auth/logout` - Cerrar sesion

#### Validaciones
- Credenciales invalidas retornan error HTTP 401
- Token expirado requiere nuevo login
- Acceso sin token retorna HTTP 403

## 3. Patrones de Diseño Implementados

### 3.1 Strategy Pattern

**Ubicacion**: `strategies/` package

**Interfaz**: `MatchScheduleStrategy`

**Implementaciones**:
1. `RoundRobinScheduleStrategy` - Liga todos contra todos
2. `SingleEliminationScheduleStrategy` - Eliminacion directa
3. `GroupStageScheduleStrategy` - Grupos y luego cruces

**Justificacion**: Permite intercambiar algoritmos de generacion de calendarios sin modificar el codigo cliente. El tipo de formato del torneo define cual estrategia usar.

### 3.2 Template Method Pattern (mediante BaseService)

**Ubicacion**: `services/BaseService.java`

**Descripcion**: Clase abstracta que define operaciones CRUD comunes para todos los servicios.

**Metodos Base**:
- `save(T entity)` - Guardar entidad
- `findById(ID id)` - Buscar por ID
- `findAll()` - Obtener todas
- `findAll(Pageable)` - Obtener paginadas
- `deleteById(ID id)` - Eliminar por ID
- `count()` - Contar registros
- `saveAll(List<T>)` - Guardar lote

**Servicios que extienden**:
- `UsuarioService extends BaseService<Usuario, Integer>`
- `EquipoService extends BaseService<Equipo, Integer>`
- `TorneoService extends BaseService<Torneo, Integer>`
- `JugadorService extends BaseService<Jugador, Integer>`
- `PartidoService extends BaseService<Partido, Integer>`
- `TablaPosicionesService extends BaseService<TablaPosiciones, Integer>`

**Justificacion**: Evita duplicacion de codigo CRUD. Cada servicio implementa la logica especifica de su dominio mientras hereda operaciones comunes.

### 3.3 Data Access Object (DAO)

**Ubicacion**: `repositories/` package

**Interfaz**: Todos extienden `BaseRepository<T, ID>` que extiende `JpaRepository`

**Repositorios**:
- `UsuarioRepository`
- `EquipoRepository`
- `TorneoRepository`
- `JugadorRepository`
- `PartidoRepository`
- `TablaPosicionesRepository`
- `RolRepository`
- `AuthTokenRepository`
- `GlobalPropertyRepository`

**Justificacion**: Encapsula logica de acceso a datos. Spring Data JPA genera automaticamente las implementaciones. Permite cambiar BD sin afectar logica de negocio.

### 3.4 Singleton Pattern

**Ubicacion**: `config/GlobalConfig.java` y `config/GlobalConfigLoader.java`

**Descripcion**: Administra propiedades globales de configuracion cargadas desde BD.

**Justificacion**: Garantiza una unica instancia de configuracion accesible en toda la aplicacion. Las propiedades pueden ser sobrescritas en tiempo de ejecucion.

## 4. Programacion Generica

### Implementacion: BaseService<T, ID>

**Ubicacion**: `services/BaseService.java`

**Descripcion**: Clase abstracta generica que proporciona operaciones CRUD para cualquier tipo de entidad.

**Parametros**:
- `<T>` - Tipo de la entidad (Usuario, Equipo, Torneo, etc.)
- `<ID>` - Tipo del identificador (Integer, Long, String)


### Implementacion: BaseRepository<T, ID>

**Ubicacion**: `repositories/BaseRepository.java`

**Herencia**: Extiende `JpaRepository<T, ID>`

**Justificacion**: Define interfaz comun para todos los repositorios. Permite agregar metodos personalizados comunes a todas las entidades.

## 5. Arquitectura MVC

La aplicacion sigue arquitectura MVC con Spring Boot. Separacion entre capas:

### Modelo (Model)
**Ubicacion**: `models/` package

**Entidades**:
- `Usuario` - Autenticacion y perfiles
- `Rol` - Roles del sistema
- `Equipo` - Equipos participantes
- `Jugador` - Jugadores en equipos
- `Torneo` - Configuracion de torneos
- `Partido` - Resultados y calendario
- `TablaPosiciones` - Metricas de rendimiento
- `AuthToken` - Tokens de sesion
- `GlobalProperty` - Propiedades configurables

**Responsabilidades**:
- Representar datos del dominio
- Mapeo a tablas BD
- Validaciones basicas con anotaciones JPA
- Relaciones entre entidades

### Vista (View)
**Ubicacion**: Controllers REST + DTOs

**Controllers**:
- `AuthController` - Autenticacion
- `UsuarioController` - Gestion de usuarios
- `RolController` - Gestion de roles
- `EquipoController` - Gestion de equipos
- `JugadorController` - Gestion de jugadores
- `TorneoController` - Gestion de torneos
- `PartidoController` - Gestion de partidos
- `TablaPosicionesController` - Consulta de clasificaciones

**DTOs** (Data Transfer Objects):
- `LoginRequest` / `LoginResponse` - Credenciales
- `CreateUsuarioDto` - Creacion de usuarios
- `UsuarioSesionDto` - Info de usuario autenticado
- `PagedResponse<T>` - Respuestas paginadas
- `ApiResponse<T>` - Envolvedor de respuestas

### Controlador (Controller)
**Ubicacion**: `controllers/` package

**Responsabilidades**:
- Recibir requests HTTP
- Validar parametros
- Delegar a servicios
- Retornar respuestas JSON
- Manejo de excepciones

### Servicios (Services)
**Ubicacion**: `services/` package

**Responsabilidades**:
- Logica de negocio
- Orquestacion de datos
- Transacciones
- Calculos complejos

## 6. Funcionalidades

### 6.1 Gestion de Usuarios
- Crear usuario con rol y credenciales
- Buscar usuario por correo
- Listar usuarios
- Actualizar usuario
- Eliminar usuario
- Asignar rol a usuario

### 6.2 Gestion de Torneos
- Crear torneo con formato (ROUND_ROBIN, SINGLE_ELIMINATION, GROUP_STAGE)
- Estados de torneo (BORRADOR, EN_PROGRESO, FINALIZADO)
- Especificar fecha de inicio
- Configurar numero de grupos (para format grupo)
- Listar torneos

### 6.3 Gestion de Equipos
- Crear equipo en torneo
- Asignar capitan (usuario)
- Listar equipos de torneo
- Actualizar informacion equipo
- Eliminar equipo

### 6.4 Gestion de Jugadores
- Crear jugador en equipo
- Asignar numero de camiseta
- Listar jugadores de equipo
- Actualizar informacion jugador
- Eliminar jugador

### 6.5 Generacion de Calendario
- Generar automaticamente partidos segun estrategia
- Algoritmo Round-Robin (todos contra todos)
- Algoritmo Single Elimination (eliminacion directa)
- Algoritmo Group Stage (grupos y cruces)
- Asignar fechas a partidos

### 6.6 Registro de Resultados
- Registrar goles equipo local y visitante
- Marcar partido como jugado
- Actualizar tabla de posiciones automaticamente

### 6.7 Tabla de Posiciones
- Calcular puntos (3 victoria, 1 empate, 0 derrota)
- Calcular goles a favor y en contra
- Diferencia de goles
- Ordenar equipos por criterios
- Tablas por grupo (en format grupo)

## 7. Base de Datos

### Motor
PostgreSQL 13+

### Tablas Principales
1. `roles` - Roles del sistema (admin, user, capitan)
2. `usuarios` - Credenciales y perfiles
3. `auth_tokens` - Tokens de sesion persistentes
4. `torneos` - Configuracion de torneos
5. `equipos` - Equipos participantes
6. `jugadores` - Roster de equipos
7. `partidos` - Calendario y resultados
8. `tabla_posiciones` - Clasificaciones
9. `global_properties` - Propiedades configurables

### Inicializacion
- Script: `sql/database.sql`
- Se ejecuta automaticamente en startup
- Crea tablas si no existen
- Poblacion inicial de roles y usuario master

## 8. Swagger

**Endpoint**: `/swagger-ui.html`

Documentacion automatica de API REST con descripcion de endpoints, parametros y respuestas.

### Variables de Entorno Requeridas
```
DB_HOST=localhost
DB_PORT=5432
DB_NAME=deportivos
DB_USERNAME=usuario
DB_PASSWORD=contraseña

MASTER_USERNAME=admin
MASTER_EMAIL=admin@example.com
MASTER_PASSWORD=password123
```

### Compilacion y Ejecucion
```bash
# Compilar
mvn clean install

# Ejecutar
mvn spring-boot:run
```

### Verificacion
- API disponible en `http://localhost:8080`
- Swagger en `http://localhost:8080/swagger-ui.html`
