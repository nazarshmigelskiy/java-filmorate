package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.Optional;

public interface FilmStorage {
    Collection<Film> getFilms();

    Film addFilm(Film film);

    Film update(Film film);

    Optional<Film> getById(Long id);

    void addLike(Long filmId, Long userId);

    void removeLike(Long filmId, Long userId);

    Collection<Film> getMostLiked(int count, Long genreId, Integer year);

    Collection<Film> getFilmsByDirector(Long directorId, String sortBy);

    Collection<Film> getRecommendations(Long userId);

    void deleteFilm(Long id);
}
