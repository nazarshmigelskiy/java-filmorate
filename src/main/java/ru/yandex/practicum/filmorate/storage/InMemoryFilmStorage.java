package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();

    @Override
    public Collection<Film> getFilms() {
        return films.values();
    }

    @Override
    public Film addFilm(Film film) {
        film.setId(getNextId(films));
        films.put(film.getId(), film);
        log.info("Создан фильм с id= {}", film.getId());
        return film;
    }

    @Override
    public Film update(Film film) {
        if (film.getId() == null) throw new ValidationException("Id должен быть указан");
        if (films.containsKey(film.getId())) {
            films.put(film.getId(), film);
            log.info("Обновлена информация о фильме с id= {}", film.getId());
            return film;
        } else {
            throw new NotFoundException("Фильм не найден");
        }
    }

    @Override
    public Film getById(Long id) {
        if (!films.containsKey(id)) throw new NotFoundException("Фильм с указанным id не найден.");
        return films.get(id);
    }

    private long getNextId(Map<Long, Film> films) {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
