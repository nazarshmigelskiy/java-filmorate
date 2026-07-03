package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

public interface FilmStorage {
    Collection<Film> getFilms();

    Film addFilm(Film film);

    Film update(Film film);

    Film getById(Long id);
}
