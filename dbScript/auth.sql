--CREATE DATABASE pswe06; database previously created as per steps in the README

CREATE TABLE usuarios (
    id              SERIAL PRIMARY KEY,
    email           VARCHAR(255) UNIQUE NOT NULL,
    clave_hash      VARCHAR(255) NOT NULL,
    intentos_fallidos INT NOT NULL DEFAULT 0,
    bloqueado       BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_creacion  TIMESTAMP DEFAULT NOW()
);

INSERT INTO usuarios (email, clave_hash)
VALUES ('usuario@ejemplo.com', 'Abc!1');