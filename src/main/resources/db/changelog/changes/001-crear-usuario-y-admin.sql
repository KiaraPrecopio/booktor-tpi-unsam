--liquibase formatted sql

--changeset kiara:001-crear-tabla-usuario
CREATE TABLE usuario (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(60) NOT NULL,
    rol VARCHAR(20) NOT NULL CHECK (rol IN ('ADMINISTRADOR', 'ESTUDIANTE', 'TUTOR')),
    duracion_turno_minutos INT NULL,
    activo BIT NOT NULL DEFAULT 1
);

--changeset kiara:002-insertar-admin-inicial
-- Los valores llegan desde el .env (ADMIN_EMAIL y ADMIN_PASSWORD_HASH); password es un hash BCrypt.
INSERT INTO usuario (nombre, apellido, email, password, rol, activo)
VALUES ('Administrador', 'Booktor', '${admin_email}', '${admin_password_hash}', 'ADMINISTRADOR', 1);
