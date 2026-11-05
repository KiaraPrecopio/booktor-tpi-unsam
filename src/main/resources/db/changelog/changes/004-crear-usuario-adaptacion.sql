--liquibase formatted sql

--changeset willy:005-crear-tabla-usuario-adaptacion
-- Relación muchos a muchos: adaptaciones preferidas (estudiante) u ofrecidas (tutor), según el rol del usuario.
CREATE TABLE usuario_adaptacion (
    usuario_id BIGINT NOT NULL,
    adaptacion_id BIGINT NOT NULL,
    CONSTRAINT pk_usuario_adaptacion PRIMARY KEY (usuario_id, adaptacion_id),
    CONSTRAINT fk_usuario_adaptacion_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id),
    CONSTRAINT fk_usuario_adaptacion_adaptacion FOREIGN KEY (adaptacion_id) REFERENCES adaptacion (id)
);
