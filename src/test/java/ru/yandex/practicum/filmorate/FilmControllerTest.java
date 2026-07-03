package ru.yandex.practicum.filmorate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class FilmControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
    }

    private Film validFilm() {
        Film film = new Film();
        film.setName("Тестовый фильм");
        film.setDescription("Описание фильма");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        return film;
    }

    private Film createFilm() throws Exception {
        String response = mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(validFilm())))
                .andReturn().getResponse().getContentAsString();
        return mapper.readValue(response, Film.class);
    }


    @Test
    @DisplayName("Создание валидного фильма")
    void createFilm_valid() throws Exception {
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(validFilm())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Тестовый фильм"));
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


    @Test
    @DisplayName("Обновление без id")
    void updateFilm_noId() throws Exception {
        Film film = validFilm();
        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Обновление несуществующего фильма")
    void updateFilm_notFound() throws Exception {
        Film film = validFilm();
        film.setId(999L);
        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(film)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Обновление существующего фильма")
    void updateFilm_success() throws Exception {
        Film created = createFilm();
        created.setName("Обновлённое название");
        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(created)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Обновлённое название"));
    }


    @Test
    @DisplayName("Получение фильма по id")
    void getFilmById_success() throws Exception {
        Film created = createFilm();
        mockMvc.perform(get("/films/" + created.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(created.getId()));
    }

    @Test
    @DisplayName("Получение фильма по несуществующему id")
    void getFilmById_notFound() throws Exception {
        mockMvc.perform(get("/films/999"))
                .andExpect(status().isNotFound());
    }


    @Test
    @DisplayName("Лайк несуществующего фильма")
    void addLike_filmNotFound() throws Exception {
        mockMvc.perform(put("/films/999/like/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Лайк от несуществующего пользователя")
    void addLike_userNotFound() throws Exception {
        Film created = createFilm();
        mockMvc.perform(put("/films/" + created.getId() + "/like/999"))
                .andExpect(status().isNotFound());
    }


    @Test
    @DisplayName("Список популярных фильмов — дефолтный count")
    void getPopular_defaultCount() throws Exception {
        mockMvc.perform(get("/films/popular"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Список популярных фильмов — кастомный count")
    void getPopular_customCount() throws Exception {
        mockMvc.perform(get("/films/popular?count=5"))
                .andExpect(status().isOk());
    }
}