-- Almacena la información de autenticación y perfil de todos los usuarios del sistema.
CREATE TABLE usuarios (
                          id SERIAL PRIMARY KEY,
                          nombre VARCHAR(255) NOT NULL,
                          correo VARCHAR(255) NOT NULL UNIQUE,
                          contrasena VARCHAR(255) NOT NULL,
                          rol VARCHAR(50) NOT NULL
);

-- Contiene los detalles de configuración para cada torneo.
CREATE TABLE torneos (
                         id SERIAL PRIMARY KEY,
                         nombre VARCHAR(255) NOT NULL,
                         tipo_formato VARCHAR(50) NOT NULL,
                         fecha_inicio DATE NOT NULL
);

-- Representa los equipos que participan en los torneos.
CREATE TABLE equipos (
                         id SERIAL PRIMARY KEY,
                         id_torneo INT REFERENCES torneos(id) ON DELETE SET NULL,
                         id_capitan INT REFERENCES usuarios(id) ON DELETE SET NULL,
                         nombre VARCHAR(255) NOT NULL
);

-- Almacena la información de los jugadores asociados a equipos específicos.
CREATE TABLE jugadores (
                           id SERIAL PRIMARY KEY,
                           id_equipo INT REFERENCES equipos(id) ON DELETE CASCADE,
                           nombre VARCHAR(255) NOT NULL,
                           numero_camiseta INT
);

-- Registra el calendario y los resultados de los partidos del torneo.
CREATE TABLE partidos (
                          id SERIAL PRIMARY KEY,
                          id_torneo INT REFERENCES torneos(id) ON DELETE CASCADE,
                          id_equipo_local INT REFERENCES equipos(id) ON DELETE CASCADE,
                          id_equipo_visitante INT REFERENCES equipos(id) ON DELETE CASCADE,
                          goles_local INT DEFAULT 0,
                          goles_visitante INT DEFAULT 0,
                          fecha_partido TIMESTAMP NOT NULL
);

-- Rastrea las métricas de rendimiento de los equipos dentro de un torneo específico.
CREATE TABLE tabla_posiciones (
                                  id SERIAL PRIMARY KEY,
                                  id_torneo INT REFERENCES torneos(id) ON DELETE CASCADE,
                                  id_equipo INT REFERENCES equipos(id) ON DELETE CASCADE,
                                  partidos_jugados INT DEFAULT 0,
                                  puntos INT DEFAULT 0,
                                  goles_a_favor INT DEFAULT 0,
                                  goles_en_contra INT DEFAULT 0
);