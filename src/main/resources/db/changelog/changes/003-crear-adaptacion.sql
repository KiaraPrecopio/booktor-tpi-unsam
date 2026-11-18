--liquibase formatted sql

--changeset kalil:004-crear-tabla-adaptacion
CREATE TABLE adaptacion (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(255) NULL,
    activo BIT NOT NULL DEFAULT 1
);