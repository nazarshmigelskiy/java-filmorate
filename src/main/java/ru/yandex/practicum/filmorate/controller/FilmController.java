package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.Collection;

@Validated
@RestController
@Slf4j
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {
    private final FilmStorage filmStorage;
    private final FilmService filmService;

    @GetMapping
    public Collection<Film> getFilms() {
        return filmStorage.getFilms();
    }

    @PostMapping
    public Film addFilm(@Valid @RequestBody Film film) {
        filmService.checkGenresAndMpa(film);
        filmService.checkDirectors(film);
        return filmStorage.addFilm(film);
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film film) {
        filmService.checkGenresAndMpa(film);
        filmService.checkDirectors(film);
        return filmStorage.update(film);
    }

    @GetMapping("/{id}")
    public Film getById(@PathVariable @Positive Long id) {
        return filmStorage.getById(id).orElseThrow(() -> new NotFoundException("Фильм не найден."));
    }

    @PutMapping("/{id}/like/{userId}")
    public Film addLike(@PathVariable @Positive Long id,
                        @PathVariable @Positive Long userId) {
        return filmService.addLike(userId, id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteFilm(@PathVariable @Positive Long id) {
        filmService.deleteFilm(id);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public Film deleteLike(@PathVariable @Positive Long id,
                           @PathVariable @Positive Long userId) {
        return filmService.removeLike(userId, id);
    }

    @GetMapping("/popular")
    public Collection<Film> getMostLiked(
            @RequestParam(defaultValue = "10") @Positive int count,
            @RequestParam(required = false) Long genreId,
            @RequestParam(required = false) Integer year) {
        return filmService.getMostLikedFilms(count, genreId, year);
    }

    @GetMapping("/director/{directorId}")
    public Collection<Film> getFilmsByDirector(
            @PathVariable Long directorId,
            @RequestParam(defaultValue = "year") String sortBy) {
        return filmService.getFilmsByDirector(directorId, sortBy);
    }

    @GetMapping("/search")
    public Collection<Film> searchFilms(
            @RequestParam String query,
            @RequestParam(required = false, defaultValue = "title") String by) {
        log.info("GET /films/search: query='{}', by='{}'", query, by);
        return filmService.searchFilms(query, by);
    }
}
