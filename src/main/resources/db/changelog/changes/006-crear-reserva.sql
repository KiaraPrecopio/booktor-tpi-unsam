--liquibase formatted sql

--changeset willy:007-crear-tabla-reserva
CREATE TABLE reserva (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    turno_id BIGINT NOT NULL,
    estudiante_id BIGINT NOT NULL,
    estado VARCHAR(20) NOT NULL CHECK (estado IN ('ACTIVA', 'CANCELADA', 'FINALIZADA')),
    fecha_reserva DATETIME2 NOT NULL,
    objetivo VARCHAR(500) NULL,
    cancelada_por VARCHAR(20) NULL CHECK (cancelada_por IN ('ESTUDIANTE', 'TUTOR', 'ADMINISTRADOR')),
    fecha_cancelacion DATETIME2 NULL,
    CONSTRAINT fk_reserva_turno FOREIGN KEY (turno_id) REFERENCES turno (id),
    CONSTRAINT fk_reserva_estudiante FOREIGN KEY (estudiante_id) REFERENCES usuario (id)
);

--changeset willy:008-indice-reserva-activa-por-turno
CREATE UNIQUE INDEX ux_reserva_turno_activa ON reserva (turno_id) WHERE estado = 'ACTIVA';
