package ru.yandex.practicum.filmorate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FilmController.class)
class FilmControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FilmStorage filmStorage;

    @MockitoBean
    private FilmService filmService;

    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
    }

    private Film validFilm() {
        Film film = new Film();
        film.setId(1L);
        film.setName("Тестовый фильм");
        film.setDescription("Описание фильма");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        return film;
    }

    // ─── POST /films ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("Создание валидного фильма")
    void createFilm_valid() throws Exception {
        Film film = validFilm();
        when(filmStorage.addFilm(any(Film.class))).thenReturn(film);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(film)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Тестовый фильм"));
    }

    @Test
    @DisplayName("Создание фильма с жанрами")
    void createFilm_withGenres() throws Exception {
        Film film = validFilm();
        Genre genre = new Genre(1L, "Комедия");
        film.setGenres(Set.of(genre));
        when(filmStorage.addFilm(any(Film.class))).thenReturn(film);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(film)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.genres").isArray())
                .andExpect(jsonPath("$.genres[0].id").value(1));
    }

    @Test
    @DisplayName("Создание фильма с рейтингом MPA")
    void createFilm_withMpa() throws Exception {
        Film film = validFilm();
        film.setMpa(new MPA(1L, "G"));
        when(filmStorage.addFilm(any(Film.class))).thenReturn(film);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(film)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mpa.id").value(1))
                .andExpect(jsonPath("$.mpa.name").value("G"));
    }

    @Test
    @DisplayName("Название null")
    void createFilm_nullName() throws Exception {
        Film film = validFilm();
        film.setName(null);
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Название из пробелов")
    void createFilm_blankName() throws Exception {
        Film film = validFilm();
        film.setName("   ");
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Описание ровно 200 символов — граница")
    void createFilm_description200chars() throws Exception {
        Film film = validFilm();
        film.setDescription("А".repeat(200));
        when(filmStorage.addFilm(any(Film.class))).thenReturn(film);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(film)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Описание 201 символ — граница+1")
    void createFilm_description201chars() throws Exception {
        Film film = validFilm();
        film.setDescription("А".repeat(201));
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Дата релиза ровно 28 декабря 1895 — граница")
    void createFilm_minReleaseDate() throws Exception {
        Film film = validFilm();
        film.setReleaseDate(LocalDate.of(1895, 12, 28));
        when(filmStorage.addFilm(any(Film.class))).thenReturn(film);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(film)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Дата релиза 27 декабря 1895 — граница-1")
    void createFilm_beforeMinReleaseDate() throws Exception {
        Film film = validFilm();
        film.setReleaseDate(LocalDate.of(1895, 12, 27));
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Длительность 1 — граница")
    void createFilm_durationOne() throws Exception {
        Film film = validFilm();
        film.setDuration(1);
        when(filmStorage.addFilm(any(Film.class))).thenReturn(film);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(film)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Длительность 0 — граница-1")
    void createFilm_zeroDuration() throws Exception {
        Film film = validFilm();
        film.setDuration(0);
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Отрицательная длительность")
    void createFilm_negativeDuration() throws Exception {
        Film film = validFilm();
        film.setDuration(-1);
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest());
    }

    // ─── PUT /films ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Обновление без id")
    void updateFilm_noId() throws Exception {
        Film film = validFilm();
        film.setId(null);
        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(film)))
                .andExpect(status().isOk());
        verify(filmStorage).update(any(Film.class));
    }

    @Test
    @DisplayName("Обновление несуществующего фильма")
    void updateFilm_notFound() throws Exception {
        Film film = validFilm();
        film.setId(999L);
        when(filmStorage.update(any(Film.class))).thenThrow(new NotFoundException("Фильм не найден."));

        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(film)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Обновление существующего фильма")
    void updateFilm_success() throws Exception {
        Film film = validFilm();
        film.setName("Обновлённое название");
        when(filmStorage.update(any(Film.class))).thenReturn(film);

        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(film)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Обновлённое название"));
    }

    // ─── GET /films ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Список фильмов")
    void getFilms_success() throws Exception {
        when(filmStorage.getFilms()).thenReturn(List.of(validFilm()));

        mockMvc.perform(get("/films"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("Получение фильма по id")
    void getFilmById_success() throws Exception {
        Film film = validFilm();
        when(filmStorage.getById(1L)).thenReturn(Optional.of(film));

        mockMvc.perform(get("/films/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("Получение фильма по несуществующему id")
    void getFilmById_notFound() throws Exception {
        when(filmStorage.getById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/films/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Некорректный id — отрицательный")
    void getFilmById_negativeId() throws Exception {
        mockMvc.perform(get("/films/-1"))
                .andExpect(status().isBadRequest());
    }

    // ─── PUT /films/{id}/like/{userId} ────────────────────────────────────────

    @Test
    @DisplayName("Успешный лайк фильма")
    void addLike_success() throws Exception {
        Film film = validFilm();
        when(filmService.addLike(1L, 1L)).thenReturn(film);

        mockMvc.perform(put("/films/1/like/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("Лайк несуществующего фильма")
    void addLike_filmNotFound() throws Exception {
        when(filmService.addLike(1L, 999L)).thenThrow(new NotFoundException("Фильм не найден."));

        mockMvc.perform(put("/films/999/like/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Лайк от несуществующего пользователя")
    void addLike_userNotFound() throws Exception {
        when(filmService.addLike(999L, 1L)).thenThrow(new NotFoundException("Пользователь не найден."));

        mockMvc.perform(put("/films/1/like/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Повторный лайк от того же пользователя")
    void addLike_duplicate() throws Exception {
        when(filmService.addLike(1L, 1L))
                .thenThrow(new ConditionsNotMetException("Пользователь с id: 1 уже оценил фильм."));

        mockMvc.perform(put("/films/1/like/1"))
                .andExpect(status().isUnprocessableEntity());
    }

    // ─── DELETE /films/{id}/like/{userId} ─────────────────────────────────────

    @Test
    @DisplayName("Успешное снятие лайка")
    void removeLike_success() throws Exception {
        Film film = validFilm();
        when(filmService.removeLike(1L, 1L)).thenReturn(film);

        mockMvc.perform(delete("/films/1/like/1"))
                .andExpect(status().isOk());
        verify(filmService).removeLike(1L, 1L);
    }

    @Test
    @DisplayName("Снятие лайка, который не был поставлен")
    void removeLike_notLiked() throws Exception {
        when(filmService.removeLike(1L, 1L))
                .thenThrow(new ConditionsNotMetException("Пользователь еще не оценил фильм."));

        mockMvc.perform(delete("/films/1/like/1"))
                .andExpect(status().isUnprocessableEntity());
    }

    // ─── GET /films/popular ───────────────────────────────────────────────────

    @Test
    @DisplayName("Список популярных фильмов — дефолтный count")
    void getPopular_defaultCount() throws Exception {
        when(filmService.getMostLikedFilms(anyInt())).thenReturn(List.of(validFilm()));

        mockMvc.perform(get("/films/popular"))
                .andExpect(status().isOk());
        verify(filmService).getMostLikedFilms(10);
    }

    @Test
    @DisplayName("Список популярных фильмов — кастомный count")
    void getPopular_customCount() throws Exception {
        when(filmService.getMostLikedFilms(anyInt())).thenReturn(List.of(validFilm()));

        mockMvc.perform(get("/films/popular?count=5"))
                .andExpect(status().isOk());
        verify(filmService).getMostLikedFilms(5);
    }

    @Test
    @DisplayName("Некорректный count — отрицательный")
    void getPopular_negativeCount() throws Exception {
        mockMvc.perform(get("/films/popular?count=-1"))
                .andExpect(status().isBadRequest());
        verify(filmService, never()).getMostLikedFilms(anyInt());
    }
}
