package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.dal.DirectorRowMapper;
import ru.yandex.practicum.filmorate.dal.FilmRowMapper;
import ru.yandex.practicum.filmorate.dal.GenreRowMapper;
import ru.yandex.practicum.filmorate.dal.UserRowMapper;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.DirectorDbStorage;
import ru.yandex.practicum.filmorate.storage.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.UserDbStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Import({FilmDbStorage.class, FilmRowMapper.class, GenreRowMapper.class,
        UserDbStorage.class, UserRowMapper.class,
        DirectorDbStorage.class, DirectorRowMapper.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class FilmDbStorageTest {

    @Autowired
    private FilmDbStorage filmStorage;
    @Autowired
    private UserDbStorage userStorage;
    @Autowired
    private DirectorDbStorage directorStorage;

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

        List<Film> popular = filmStorage.getMostLiked(10, null, null);

        assertThat(popular).isNotEmpty();
        assertThat(popular.get(0).getId()).isEqualTo(film2.getId());
    }

    @Test
    @DisplayName("Популярные фильмы — фильтрация по жанру")
    void getMostLikedByGenre() {
        Genre genre1 = new Genre();
        genre1.setId(1L);
        Genre genre2 = new Genre();
        genre2.setId(2L);
        User user = userStorage.create(makeUser());

        Film filmWithLike = makeFilm();
        filmWithLike.setGenres(Set.of(genre1));
        filmStorage.addFilm(filmWithLike);
        filmStorage.addLike(filmWithLike.getId(), user.getId());

        Film otherFilm = makeFilm();
        otherFilm.setGenres(Set.of(genre2));
        filmStorage.addFilm(otherFilm);

        List<Film> popular = filmStorage.getMostLiked(10, 1L, null);

        assertThat(popular).hasSize(1);
        assertThat(popular.get(0).getId()).isEqualTo(filmWithLike.getId());
    }

    @Test
    @DisplayName("Популярные фильмы — фильтрация по году")
    void getMostLikedByYear() {
        User user = userStorage.create(makeUser());

        Film film2000 = makeFilm();
        film2000.setReleaseDate(LocalDate.of(2000, 6, 15));
        filmStorage.addFilm(film2000);
        filmStorage.addLike(film2000.getId(), user.getId());

        Film film2001 = makeFilm();
        film2001.setReleaseDate(LocalDate.of(2001, 1, 1));
        filmStorage.addFilm(film2001);

        List<Film> popular = filmStorage.getMostLiked(10, null, 2000);

        assertThat(popular).hasSize(1);
        assertThat(popular.get(0).getId()).isEqualTo(film2000.getId());
    }

    @Test
    @DisplayName("Создание фильма с режиссёрами")
    void createFilmWithDirectors() {
        Director director = directorStorage.create(makeDirector());

        Film film = makeFilm();
        film.setDirectors(Set.of(director));
        Film created = filmStorage.addFilm(film);

        assertThat(filmStorage.getById(created.getId()))
                .isPresent()
                .hasValueSatisfying(f -> assertThat(f.getDirectors())
                        .extracting(Director::getId)
                        .containsExactly(director.getId()));
    }

    @Test
    @DisplayName("Фильмы режиссёра — сортировка по году")
    void getFilmsByDirector_sortedByYear() {
        Director director = directorStorage.create(makeDirector());

        Film film1 = makeFilm();
        film1.setReleaseDate(LocalDate.of(2010, 1, 1));
        film1.setDirectors(Set.of(director));
        filmStorage.addFilm(film1);

        Film film2 = makeFilm();
        film2.setReleaseDate(LocalDate.of(2000, 1, 1));
        film2.setDirectors(Set.of(director));
        filmStorage.addFilm(film2);

        List<Film> films = filmStorage.getFilmsByDirector(director.getId(), "year");

        assertThat(films).hasSize(2);
        assertThat(films.get(0).getId()).isEqualTo(film2.getId());
        assertThat(films.get(1).getId()).isEqualTo(film1.getId());
    }

    @Test
    @DisplayName("Фильмы режиссёра — сортировка по лайкам")
    void getFilmsByDirector_sortedByLikes() {
        Director director = directorStorage.create(makeDirector());
        User user = userStorage.create(makeUser());

        Film film1 = makeFilm();
        film1.setDirectors(Set.of(director));
        filmStorage.addFilm(film1);

        Film film2 = makeFilm();
        film2.setDirectors(Set.of(director));
        filmStorage.addFilm(film2);

        filmStorage.addLike(film2.getId(), user.getId());

        List<Film> films = filmStorage.getFilmsByDirector(director.getId(), "likes");

        assertThat(films).hasSize(2);
        assertThat(films.get(0).getId()).isEqualTo(film2.getId());
    }

    private Director makeDirector() {
        Director director = new Director();
        director.setName("Режиссер");
        return director;
    }
}