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

-- Almacena tokens de autenticación persistentes (sin expiración de memoria en reinicio).
CREATE TABLE IF NOT EXISTS auth_tokens (
    token_id SERIAL PRIMARY KEY,
    usuarios_id INT NOT NULL REFERENCES usuarios (usuarios_id) ON DELETE CASCADE,
    token_value VARCHAR(512) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
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

-- Migraciones posteriores
ALTER TABLE torneos
ADD COLUMN IF NOT EXISTS estado VARCHAR(20) NOT NULL DEFAULT 'BORRADOR';

UPDATE torneos SET estado = 'BORRADOR' WHERE estado IS NULL;

ALTER TABLE torneos ADD COLUMN IF NOT EXISTS num_grupos INT;

ALTER TABLE partidos ADD COLUMN IF NOT EXISTS grupo VARCHAR(10);

ALTER TABLE partidos ADD COLUMN IF NOT EXISTS bracket_index INT;

ALTER TABLE partidos ADD COLUMN IF NOT EXISTS fase VARCHAR(255);

ALTER TABLE tabla_posiciones
ADD COLUMN IF NOT EXISTS grupo VARCHAR(10);

ALTER TABLE tabla_posiciones
ADD COLUMN IF NOT EXISTS victorias INT NOT NULL DEFAULT 0;

ALTER TABLE tabla_posiciones
ADD COLUMN IF NOT EXISTS empates INT NOT NULL DEFAULT 0;

ALTER TABLE tabla_posiciones
ADD COLUMN IF NOT EXISTS derrotas INT NOT NULL DEFAULT 0;

ALTER TABLE partidos
DROP CONSTRAINT IF EXISTS partidos_id_equipo_local_fkey;

ALTER TABLE partidos
DROP CONSTRAINT IF EXISTS partidos_id_equipo_visitante_fkey;

ALTER TABLE partidos
ADD CONSTRAINT partidos_id_equipo_local_fkey FOREIGN KEY (id_equipo_local) REFERENCES equipos (equipos_id) ON DELETE CASCADE;

ALTER TABLE partidos
ADD CONSTRAINT partidos_id_equipo_visitante_fkey FOREIGN KEY (id_equipo_visitante) REFERENCES equipos (equipos_id) ON DELETE CASCADE;