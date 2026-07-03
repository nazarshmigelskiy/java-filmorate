package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Comparator;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class FilmService {
    private final FilmStorage storage;
    private final UserStorage userStorage;

    public Film addLike(Long userId, Long filmId) {
        userStorage.getById(userId);
        Film film = storage.getById(filmId);
        if (film.getLikes().contains(userId)) throw new ConditionsNotMetException("Пользователь уже оценил фильм.");
        film.getLikes().add(userId);
        log.info("Пользователь с id {} поставил лайк фильму с  id {}", userId, filmId);
        return film;
    }

    public Film removeLike(Long userId, Long filmId) {
        userStorage.getById(userId);
        Film film = storage.getById(filmId);
        if (!film.getLikes().contains(userId)) throw new ConditionsNotMetException("Пользователь еще не оценил фильм.");
        film.getLikes().remove(userId);
        log.info("Пользователь с id {} убрал лайк фильму с id {}", userId, filmId);
        return film;
    }

    public List<Film> getMostLikedFilms(int count) {
        return storage.getFilms().stream()
                .sorted(Comparator.comparingInt((Film film) -> film.getLikes().size()).reversed())
                .limit(count)
                .toList();
    }
}
