DELETE FROM directors;
ALTER TABLE directors ALTER COLUMN id RESTART WITH 1;
DELETE FROM films;
ALTER TABLE films ALTER COLUMN id RESTART WITH 1;

MERGE INTO genres (id, name) VALUES (1, 'Комедия');
MERGE INTO genres (id, name) VALUES (2, 'Драма');
MERGE INTO genres (id, name) VALUES (3, 'Мультфильм');
MERGE INTO genres (id, name) VALUES (4, 'Триллер');
MERGE INTO genres (id, name) VALUES (5, 'Документальный');
MERGE INTO genres (id, name) VALUES (6, 'Боевик');

MERGE INTO mpa (id, name) VALUES (1, 'G');
MERGE INTO mpa (id, name) VALUES (2, 'PG');
MERGE INTO mpa (id, name) VALUES (3, 'PG-13');
MERGE INTO mpa (id, name) VALUES (4, 'R');
MERGE INTO mpa (id, name) VALUES (5, 'NC-17');