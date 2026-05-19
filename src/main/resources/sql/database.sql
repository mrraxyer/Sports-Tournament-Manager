-- Define los roles disponibles en el sistema.
CREATE TABLE IF NOT EXISTS roles (
    roles_id SERIAL PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE
);

-- Tabla para almacenar propiedades globales que pueden sobreescribir application.properties
CREATE TABLE IF NOT EXISTS global_properties (
    id SERIAL PRIMARY KEY,
    prop_key VARCHAR(255) NOT NULL UNIQUE,
    prop_value TEXT
);

-- Almacena la información de autenticación y perfil de todos los usuarios del sistema.
CREATE TABLE IF NOT EXISTS usuarios (
    usuarios_id SERIAL PRIMARY KEY,
    roles_id INT REFERENCES roles (roles_id) ON DELETE SET NULL,
    nombre VARCHAR(255) NOT NULL,
    correo VARCHAR(255) NOT NULL UNIQUE,
    contrasena VARCHAR(255) NOT NULL
);

-- Contiene los detalles de configuración para cada torneo.
CREATE TABLE IF NOT EXISTS torneos (
    torneos_id SERIAL PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    tipo_formato VARCHAR(50) NOT NULL,
    fecha_inicio DATE NOT NULL
);

-- Representa los equipos que participan en los torneos.
CREATE TABLE IF NOT EXISTS equipos (
    equipos_id SERIAL PRIMARY KEY,
    torneos_id INT REFERENCES torneos (torneos_id) ON DELETE SET NULL,
    id_capitan INT REFERENCES usuarios (usuarios_id) ON DELETE SET NULL,
    nombre VARCHAR(255) NOT NULL
);

-- Almacena la información de los jugadores asociados a equipos específicos.
CREATE TABLE IF NOT EXISTS jugadores (
    jugadores_id SERIAL PRIMARY KEY,
    equipos_id INT REFERENCES equipos (equipos_id) ON DELETE CASCADE,
    nombre VARCHAR(255) NOT NULL,
    numero_camiseta INT
);

-- Registra el calendario y los resultados de los partidos del torneo.
CREATE TABLE IF NOT EXISTS partidos (
    partidos_id SERIAL PRIMARY KEY,
    torneos_id INT REFERENCES torneos (torneos_id) ON DELETE CASCADE,
    id_equipo_local INT REFERENCES equipos (equipos_id) ON DELETE CASCADE,
    id_equipo_visitante INT REFERENCES equipos (equipos_id) ON DELETE CASCADE,
    goles_local INT DEFAULT 0,
    goles_visitante INT DEFAULT 0,
    jugado BOOLEAN DEFAULT FALSE,
    fecha_partido TIMESTAMP NOT NULL
);

-- Rastrea las métricas de rendimiento de los equipos dentro de un torneo específico.
CREATE TABLE IF NOT EXISTS tabla_posiciones (
    tabla_posiciones_id SERIAL PRIMARY KEY,
    torneos_id INT REFERENCES torneos (torneos_id) ON DELETE CASCADE,
    equipos_id INT REFERENCES equipos (equipos_id) ON DELETE CASCADE,
    partidos_jugados INT DEFAULT 0,
    puntos INT DEFAULT 0,
    goles_a_favor INT DEFAULT 0,
    goles_en_contra INT DEFAULT 0
);