--liquibase formatted sql

--changeset kiara:003-crear-tabla-materia
CREATE TABLE materia (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(255) NULL,
    activo BIT NOT NULL DEFAULT 1
);
