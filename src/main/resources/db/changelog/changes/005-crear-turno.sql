--liquibase formatted sql

--changeset willy:006-crear-tabla-turno
CREATE TABLE turno (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    tutor_id BIGINT NOT NULL,
    materia_id BIGINT NOT NULL,
    inicio DATETIME2 NOT NULL,
    fin DATETIME2 NOT NULL,
    estado VARCHAR(20) NOT NULL CHECK (estado IN ('DISPONIBLE', 'ASIGNADO', 'FINALIZADO', 'DADO_DE_BAJA')),
    modalidad VARCHAR(20) NOT NULL CHECK (modalidad IN ('VIRTUAL', 'PRESENCIAL')),
    link VARCHAR(255) NULL,
    lugar VARCHAR(255) NULL,
    CONSTRAINT fk_turno_tutor FOREIGN KEY (tutor_id) REFERENCES usuario (id),
    CONSTRAINT fk_turno_materia FOREIGN KEY (materia_id) REFERENCES materia (id)
);
