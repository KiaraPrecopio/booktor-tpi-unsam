--liquibase formatted sql

--changeset willy:009-datos-demo context:demo
-- Datos de ejemplo para la demo. Contraseña de todos los usuarios: Test1234.
INSERT INTO adaptacion (nombre, descripcion, activo) VALUES
('Material visual', 'Gráficos, diagramas y mapas conceptuales', 1),
('Lectura fácil', 'Textos cortos con lenguaje simple', 1),
('Subtítulos', 'Videos y reuniones con subtítulos o transcripción', 1),
('Tiempo extendido', 'Más tiempo para resolver ejercicios', 1);

INSERT INTO materia (nombre, descripcion, activo) VALUES
('Programación III', 'Java, Spring Boot y bases de datos', 1),
('Diseño de Software', 'Patrones de diseño y diagramas UML', 1),
('Proyecto Integrador II', 'Desarrollo de un proyecto de software en equipo', 1),
('Inglés técnico', 'Lectura de textos técnicos en inglés', 1),
('Bases de datos', 'Modelo relacional y SQL', 1);

INSERT INTO usuario (nombre, apellido, email, password, rol, duracion_turno_minutos, activo) VALUES
('Lucía', 'Fernández', 'tutor@booktor.com', '$2a$12$N0/qzyH7GoOuA64rNGNdkOK1b0yHJGm8wFcPycCfOyi6I/oiDM0za', 'TUTOR', 60, 1),
('Martín', 'Gómez', 'tutor2@booktor.com', '$2a$12$N0/qzyH7GoOuA64rNGNdkOK1b0yHJGm8wFcPycCfOyi6I/oiDM0za', 'TUTOR', 45, 1),
('Sofía', 'Ruiz', 'tutor3@booktor.com', '$2a$12$N0/qzyH7GoOuA64rNGNdkOK1b0yHJGm8wFcPycCfOyi6I/oiDM0za', 'TUTOR', 30, 1),
('Juan', 'Pérez', 'estudiante@booktor.com', '$2a$12$N0/qzyH7GoOuA64rNGNdkOK1b0yHJGm8wFcPycCfOyi6I/oiDM0za', 'ESTUDIANTE', NULL, 1),
('Tomás', 'Díaz', 'estudiante2@booktor.com', '$2a$12$N0/qzyH7GoOuA64rNGNdkOK1b0yHJGm8wFcPycCfOyi6I/oiDM0za', 'ESTUDIANTE', NULL, 1);

INSERT INTO usuario_adaptacion (usuario_id, adaptacion_id)
SELECT u.id, a.id
FROM (VALUES
    ('tutor@booktor.com', 'Material visual'),
    ('tutor@booktor.com', 'Lectura fácil'),
    ('tutor2@booktor.com', 'Subtítulos'),
    ('tutor3@booktor.com', 'Material visual'),
    ('tutor3@booktor.com', 'Tiempo extendido'),
    ('estudiante@booktor.com', 'Material visual'),
    ('estudiante2@booktor.com', 'Tiempo extendido')
) v (email, adaptacion)
JOIN usuario u ON u.email = v.email
JOIN adaptacion a ON a.nombre = v.adaptacion;

INSERT INTO turno (tutor_id, materia_id, inicio, fin, estado, modalidad, link, lugar)
SELECT u.id, m.id,
    DATEADD(MINUTE, v.minuto, DATEADD(DAY, v.dia, CAST(CAST(GETDATE() AS DATE) AS DATETIME2))),
    DATEADD(MINUTE, v.minuto + u.duracion_turno_minutos, DATEADD(DAY, v.dia, CAST(CAST(GETDATE() AS DATE) AS DATETIME2))),
    v.estado, v.modalidad, v.link, v.lugar
FROM (VALUES
    ('tutor@booktor.com', 'Programación III', 2, 840, 'ASIGNADO', 'VIRTUAL', 'https://meet.google.com/bkt-prog-iii', NULL),
    ('tutor@booktor.com', 'Programación III', 2, 900, 'DISPONIBLE', 'VIRTUAL', 'https://meet.google.com/bkt-prog-iii', NULL),
    ('tutor@booktor.com', 'Programación III', 2, 960, 'DISPONIBLE', 'VIRTUAL', 'https://meet.google.com/bkt-prog-iii', NULL),
    ('tutor@booktor.com', 'Diseño de Software', 3, 600, 'DISPONIBLE', 'PRESENCIAL', NULL, 'Campus Miguelete, aula 12'),
    ('tutor@booktor.com', 'Diseño de Software', 3, 660, 'DISPONIBLE', 'PRESENCIAL', NULL, 'Campus Miguelete, aula 12'),
    ('tutor2@booktor.com', 'Programación III', 2, 1080, 'ASIGNADO', 'PRESENCIAL', NULL, 'Laboratorio 3, edificio Tornavía'),
    ('tutor2@booktor.com', 'Programación III', 2, 1125, 'DISPONIBLE', 'PRESENCIAL', NULL, 'Laboratorio 3, edificio Tornavía'),
    ('tutor2@booktor.com', 'Inglés técnico', 4, 540, 'DISPONIBLE', 'VIRTUAL', 'https://meet.google.com/bkt-ingles', NULL),
    ('tutor2@booktor.com', 'Inglés técnico', 4, 585, 'DISPONIBLE', 'VIRTUAL', 'https://meet.google.com/bkt-ingles', NULL),
    ('tutor3@booktor.com', 'Programación III', 3, 960, 'ASIGNADO', 'VIRTUAL', 'https://meet.google.com/bkt-sofia', NULL),
    ('tutor3@booktor.com', 'Programación III', 3, 990, 'DISPONIBLE', 'VIRTUAL', 'https://meet.google.com/bkt-sofia', NULL),
    ('tutor3@booktor.com', 'Programación III', 3, 1020, 'DISPONIBLE', 'VIRTUAL', 'https://meet.google.com/bkt-sofia', NULL),
    ('tutor3@booktor.com', 'Proyecto Integrador II', 5, 660, 'DISPONIBLE', 'PRESENCIAL', NULL, 'Campus Miguelete, aula 5'),
    ('tutor3@booktor.com', 'Proyecto Integrador II', 5, 690, 'DISPONIBLE', 'PRESENCIAL', NULL, 'Campus Miguelete, aula 5')
) v (email, materia, dia, minuto, estado, modalidad, link, lugar)
JOIN usuario u ON u.email = v.email
JOIN materia m ON m.nombre = v.materia;

INSERT INTO reserva (turno_id, estudiante_id, estado, fecha_reserva, objetivo, cancelada_por, fecha_cancelacion)
SELECT t.id, e.id, v.estado, GETDATE(), v.objetivo, v.cancelada_por,
    CASE WHEN v.estado = 'CANCELADA' THEN GETDATE() END
FROM (VALUES
    ('tutor@booktor.com', 2, 840, 'estudiante@booktor.com', 'ACTIVA', 'Repasar herencia y polimorfismo', NULL),
    ('tutor@booktor.com', 2, 900, 'estudiante2@booktor.com', 'CANCELADA', 'Entender las anotaciones de JPA', 'ESTUDIANTE'),
    ('tutor2@booktor.com', 2, 1080, 'estudiante@booktor.com', 'ACTIVA', 'Practicar consultas con Spring Data', NULL),
    ('tutor3@booktor.com', 3, 960, 'estudiante2@booktor.com', 'ACTIVA', NULL, NULL)
) v (tutor, dia, minuto, estudiante, estado, objetivo, cancelada_por)
JOIN usuario tu ON tu.email = v.tutor
JOIN turno t ON t.tutor_id = tu.id
    AND t.inicio = DATEADD(MINUTE, v.minuto, DATEADD(DAY, v.dia, CAST(CAST(GETDATE() AS DATE) AS DATETIME2)))
JOIN usuario e ON e.email = v.estudiante;
