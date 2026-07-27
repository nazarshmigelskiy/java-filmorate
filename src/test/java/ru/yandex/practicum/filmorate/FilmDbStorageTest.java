package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.dal.FilmRowMapper;
import ru.yandex.practicum.filmorate.dal.GenreRowMapper;
import ru.yandex.practicum.filmorate.dal.UserRowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.UserDbStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Import({FilmDbStorage.class, FilmRowMapper.class, GenreRowMapper.class,
        UserDbStorage.class, UserRowMapper.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class FilmDbStorageTest {

    @Autowired
    private FilmDbStorage filmStorage;
    @Autowired
    private UserDbStorage userStorage;

    private Film makeFilm() {
        Film film = new Film();
        film.setName("Тестовый фильм");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        MPA mpa = new MPA();
        mpa.setId(1L);
        film.setMpa(mpa);
        return film;
    }

    private User makeUser() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testlogin");
        user.setName("Тест");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return user;
    }

    @Test
    @DisplayName("Создание и получение фильма по id")
    void createAndGetById() {
        Film created = filmStorage.addFilm(makeFilm());

        assertThat(filmStorage.getById(created.getId()))
                .isPresent()
                .hasValueSatisfying(film -> {
                    assertThat(film.getName()).isEqualTo("Тестовый фильм");
                    assertThat(film.getMpa().getId()).isEqualTo(1L);
                    assertThat(film.getMpa().getName()).isEqualTo("G");
                });
    }

    @Test
    @DisplayName("Создание фильма с жанрами")
    void createFilmWithGenres() {
        Film film = makeFilm();
        Genre genre = new Genre();
        genre.setId(1L);
        film.setGenres(Set.of(genre));

        Film created = filmStorage.addFilm(film);

        assertThat(filmStorage.getById(created.getId()))
                .isPresent()
                .hasValueSatisfying(f -> assertThat(f.getGenres()).hasSize(1));
    }

    @Test
    @DisplayName("Получение несуществующего фильма")
    void getById_notFound() {
        assertThat(filmStorage.getById(999L)).isEmpty();
    }

    @Test
    @DisplayName("Обновление фильма")
    void updateFilm() {
        Film created = filmStorage.addFilm(makeFilm());
        created.setName("Новое название");

        filmStorage.update(created);

        assertThat(filmStorage.getById(created.getId()))
                .isPresent()
                .hasValueSatisfying(f -> assertThat(f.getName()).isEqualTo("Новое название"));
    }

    @Test
    @DisplayName("Добавление лайка")
    void addLike() {
        Film film = filmStorage.addFilm(makeFilm());
        User user = userStorage.create(makeUser());

        filmStorage.addLike(film.getId(), user.getId());

        assertThat(filmStorage.getById(film.getId()))
                .isPresent()
                .hasValueSatisfying(f -> assertThat(f.getLikes()).contains(user.getId()));
    }

    @Test
    @DisplayName("Удаление лайка")
    void removeLike() {
        Film film = filmStorage.addFilm(makeFilm());
        User user = userStorage.create(makeUser());
        filmStorage.addLike(film.getId(), user.getId());

        filmStorage.removeLike(film.getId(), user.getId());

        assertThat(filmStorage.getById(film.getId()))
                .isPresent()
                .hasValueSatisfying(f -> assertThat(f.getLikes()).isEmpty());
    }

    @Test
    @DisplayName("Популярные фильмы отсортированы по лайкам")
    void getMostLiked() {
        Film film1 = filmStorage.addFilm(makeFilm());
        Film film2 = filmStorage.addFilm(makeFilm());
        User user = userStorage.create(makeUser());

        filmStorage.addLike(film2.getId(), user.getId());

        List<Film> popular = filmStorage.getMostLiked(10);

        assertThat(popular).isNotEmpty();
        assertThat(popular.get(0).getId()).isEqualTo(film2.getId());
    }
}