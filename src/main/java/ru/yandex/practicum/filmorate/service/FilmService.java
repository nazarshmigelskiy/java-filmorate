package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.storage.MPAStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class FilmService {
    private final FilmStorage storage;
    private final UserStorage userStorage;
    private final MPAStorage mpaStorage;
    private final GenreStorage genreStorage;
    private final DirectorStorage directorStorage;
    private final EventService eventService;

    public Film addLike(Long userId, Long filmId) {
        getUserByIdOrThrow(userId);
        Film film = storage.getById(filmId).orElseThrow(() -> new NotFoundException("Фильм не найден."));
        storage.addLike(filmId, userId);
        eventService.addEvent(userId, EventType.LIKE, Operation.ADD, filmId);
        log.info("Пользователь с id {} поставил лайк фильму с  id {}", userId, filmId);
        return film;
    }

    public Film removeLike(Long userId, Long filmId) {
        getUserByIdOrThrow(userId);
        Film film = storage.getById(filmId).orElseThrow(() -> new NotFoundException("Фильм не найден."));
        storage.removeLike(filmId, userId);
        eventService.addEvent(userId, EventType.LIKE, Operation.REMOVE, filmId);
        log.info("Пользователь с id {} убрал лайк фильму с id {}", userId, filmId);
        return film;
    }

    public void deleteFilm(Long id) {
        storage.getById(id)
                .orElseThrow(() -> new NotFoundException(String.format("Фильм с id %d не найден", id)));
        storage.deleteFilm(id);
    }

    public Collection<Film> getMostLikedFilms(int count, Long genreId, Integer year) {
        return storage.getMostLiked(count, genreId, year);
    }

    public void checkGenresAndMpa(Film film) {
        if (film.getMpa() != null) {
            Long mpaId = film.getMpa().getId();
            mpaStorage.getById(mpaId)
                    .orElseThrow(() -> new NotFoundException("MPA с id " + mpaId + " не найден"));
        }
        if (film.getGenres() == null || film.getGenres().isEmpty()) {
            return;
        }
        Set<Long> requested = film.getGenres().stream()
                .map(Genre::getId)
                .collect(Collectors.toSet());
        requested.removeAll(genreStorage.findExistingIds(requested));
        if (!requested.isEmpty()) {
            throw new NotFoundException("Жанры не найдены: " + requested);
        }
    }

    public void checkDirectors(Film film) {
        if (film.getDirectors() == null || film.getDirectors().isEmpty()) {
            return;
        }
        Set<Long> requested = film.getDirectors().stream()
                .map(Director::getId)
                .collect(Collectors.toSet());
        Set<Long> existing = new HashSet<>(directorStorage.findExistingIds(requested));
        requested.removeAll(existing);
        if (!requested.isEmpty()) {
            throw new NotFoundException("Режиссёры не найдены: " + requested);
        }
    }

    public Collection<Film> getFilmsByDirector(Long directorId, String sortBy) {
        if (sortBy != null
                && !sortBy.equalsIgnoreCase("year")
                && !sortBy.equalsIgnoreCase("likes")) {
            throw new ValidationException("sortBy может быть только 'year' или 'likes'");
        }
        directorStorage.getById(directorId)
                .orElseThrow(() -> new NotFoundException("Режиссёр с id " + directorId + " не найден"));
        return storage.getFilmsByDirector(directorId, sortBy);
    }

    public Collection<Film> getCommonFilms(Long userId, Long friendId) {
        getUserByIdOrThrow(userId);
        getUserByIdOrThrow(friendId);
        return storage.getCommonFilms(userId, friendId);
    }

    public Collection<Film> searchFilms(String query, String by) {
        if (query == null || query.isBlank()) {
            throw new ValidationException("Query не может быть пустым");
        }
        if (by == null || by.isBlank()) {
            by = "title";
        }
        return storage.searchFilms(query, by);
    }

    private void getUserByIdOrThrow(Long userId) {
        userStorage.getById(userId).orElseThrow(() ->
                new NotFoundException("Пользователь с id " + userId + " не найден"));
    }
}

