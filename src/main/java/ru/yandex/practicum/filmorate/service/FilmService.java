package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.storage.MPAStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;

@Slf4j
@RequiredArgsConstructor
@Service
public class FilmService {
    private final FilmStorage storage;
    private final UserStorage userStorage;
    private final MPAStorage mpaStorage;
    private final GenreStorage genreStorage;

    public Film addLike(Long userId, Long filmId) {
        userStorage.getById(userId);
        Film film = storage.getById(filmId).orElseThrow(() -> new NotFoundException("Фильм не найден."));
        if (film.getLikes().contains(userId))
            throw new ConditionsNotMetException(String.format("Пользователь с id: %s уже оценил фильм.", userId));
        storage.addLike(filmId, userId);
        log.info("Пользователь с id {} поставил лайк фильму с  id {}", userId, filmId);
        return film;
    }

    public Film removeLike(Long userId, Long filmId) {
        userStorage.getById(userId);
        Film film = storage.getById(filmId).orElseThrow(() -> new NotFoundException("Фильм не найден."));
        if (!film.getLikes().contains(userId)) throw new ConditionsNotMetException("Пользователь еще не оценил фильм.");
        storage.removeLike(filmId, userId);
        log.info("Пользователь с id {} убрал лайк фильму с id {}", userId, filmId);
        return film;
    }

    public void deleteFilm(Long id) {
        storage.getById(id)
                .orElseThrow(() -> new NotFoundException(String.format("Фильм с id %d не найден", id)));
        storage.deleteFilm(id);
    }

    public Collection<Film> getMostLikedFilms(int count) {
        return storage.getMostLiked(count);
    }

    public void checkGenresAndMpa(Film film) {
        if (film.getMpa() != null) {
            mpaStorage.getById(film.getMpa().getId())
                    .orElseThrow(() -> new NotFoundException("MPA с id " + film.getMpa().getId() + " не найден"));
        }
        if (film.getGenres() != null) {
            for (Genre genre : film.getGenres()) {
                genreStorage.getById(genre.getId())
                        .orElseThrow(() -> new NotFoundException("Жанр с id " + genre.getId() + " не найден"));
            }
        }
    }
}
