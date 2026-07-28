package ru.yandex.practicum.filmorate.storage;

import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.GenreRowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

@Repository
@Primary
public class FilmDbStorage extends BaseStorage<Film> implements FilmStorage {

    private static final String FIND_ALL_QUERY =
            "SELECT f.*, m.name AS mpa_name FROM films f " +
                    "LEFT JOIN mpa m ON f.mpa_id = m.id";
    private static final String FIND_BY_ID_QUERY =
            "SELECT f.*, m.name AS mpa_name FROM films f " +
                    "LEFT JOIN mpa m ON f.mpa_id = m.id WHERE f.id = ?";
    private static final String INSERT_QUERY =
            "INSERT INTO films (name, description, release_date, duration, mpa_id) " +
                    "VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE_QUERY =
            "UPDATE films SET name = ?, description = ?, release_date = ?, " +
                    "duration = ?, mpa_id = ? WHERE id = ?";
    private static final String INSERT_GENRE_QUERY =
            "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
    private static final String DELETE_GENRES_QUERY =
            "DELETE FROM film_genres WHERE film_id = ?";
    private static final String GET_GENRES_QUERY =
            "SELECT g.* FROM genres g " +
                    "JOIN film_genres fg ON g.id = fg.genre_id " +
                    "WHERE fg.film_id = ? ORDER BY g.id";
    private static final String ADD_LIKE_QUERY =
            "INSERT INTO likes (film_id, user_id) VALUES (?, ?)";
    private static final String REMOVE_LIKE_QUERY =
            "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
    private static final String GET_LIKES_QUERY =
            "SELECT user_id FROM likes WHERE film_id = ?";
    private static final String GET_POPULAR_QUERY =
            "SELECT f.*, m.name AS mpa_name, COUNT(l.user_id) AS likes_count " +
                    "FROM films f " +
                    "LEFT JOIN mpa m ON f.mpa_id = m.id " +
                    "LEFT JOIN likes l ON f.id = l.film_id " +
                    "GROUP BY f.id, m.name " +
                    "ORDER BY likes_count DESC " +
                    "LIMIT ?";
    private static final String GET_ALL_GENRES_QUERY =
            "SELECT fg.film_id, g.id, g.name FROM genres g " +
                    "JOIN film_genres fg ON g.id = fg.genre_id " +
                    "WHERE fg.film_id IN (%s) ORDER BY g.id";
    private static final String GET_ALL_LIKES_QUERY =
            "SELECT film_id, user_id FROM likes WHERE film_id IN (%s)";

    private static final String FIND_SIMILAR_USER_QUERY =
            "SELECT l2.user_id " +
                    "FROM likes l1 " +
                    "JOIN likes l2 ON l1.film_id = l2.film_id AND l1.user_id != l2.user_id " +
                    "WHERE l1.user_id = ? " +
                    "GROUP BY l2.user_id " +
                    "ORDER BY COUNT(*) DESC " +
                    "LIMIT 1";
    private static final String RECOMMENDATIONS_QUERY =
            "SELECT f.*, m.name AS mpa_name " +
                    "FROM films f " +
                    "LEFT JOIN mpa m ON f.mpa_id = m.id " +
                    "WHERE f.id IN " +
                    "(SELECT film_id FROM likes WHERE user_id = ?) " +
                    "AND f.id NOT IN " +
                    "(SELECT film_id FROM likes WHERE user_id = ?)";

    private final GenreRowMapper genreMapper;


    public FilmDbStorage(JdbcTemplate jdbc, RowMapper<Film> mapper, GenreRowMapper genreMapper) {
        super(jdbc, mapper);
        this.genreMapper = genreMapper;
    }

    @Override
    public Collection<Film> getFilms() {
        List<Film> films = findMany(FIND_ALL_QUERY);
        if (films.isEmpty()) {
            return films;
        }
        return setLikesAndGenresForFilms(films);
    }

    @Override
    public Optional<Film> getById(Long id) {
        Optional<Film> film = findOne(FIND_BY_ID_QUERY, id);
        film.ifPresent(f -> {
            f.setGenres(loadGenres(f.getId()));
            f.setLikes(loadLikes(f.getId()));
        });
        return film;
    }

    @Override
    public Film addFilm(Film film) {
        long id = insert(INSERT_QUERY,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa() != null ? film.getMpa().getId() : null);
        film.setId(id);
        saveGenres(film);
        film.setGenres(loadGenres(id));
        return getById(id).orElseThrow();
    }

    @Override
    public Film update(Film film) {
        update(UPDATE_QUERY,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa() != null ? film.getMpa().getId() : null,
                film.getId());
        saveGenres(film);
        film.setGenres(loadGenres(film.getId()));
        return getById(film.getId()).orElseThrow();
    }


    private void saveGenres(Film film) {
        delete(DELETE_GENRES_QUERY, film.getId());
        if (film.getGenres() == null || film.getGenres().isEmpty()) {
            return;
        }
        List<Genre> genres = new ArrayList<>(film.getGenres());
        jdbc.batchUpdate(INSERT_GENRE_QUERY, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setLong(1, film.getId());
                ps.setLong(2, genres.get(i).getId());
            }

            @Override
            public int getBatchSize() {
                return genres.size();
            }
        });
    }

    private Set<Genre> loadGenres(Long filmId) {
        return new LinkedHashSet<>(jdbc.query(GET_GENRES_QUERY, genreMapper, filmId));
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        jdbc.update(ADD_LIKE_QUERY, filmId, userId);
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        jdbc.update(REMOVE_LIKE_QUERY, filmId, userId);
    }

    private Set<Long> loadLikes(Long filmId) {
        return new HashSet<>(jdbc.queryForList(GET_LIKES_QUERY, Long.class, filmId));
    }

    public List<Film> getMostLiked(int count) {
        List<Film> films = findMany(GET_POPULAR_QUERY, count);
        if (films.isEmpty()) {
            return films;
        }
        return setLikesAndGenresForFilms(films);
    }

    private Map<Long, Set<Genre>> loadGenresForFilms(List<Long> filmIds) {
        if (filmIds.isEmpty()) {
            return Map.of();
        }
        String placeholders = String.join(",", Collections.nCopies(filmIds.size(), "?"));
        String sql = String.format(GET_ALL_GENRES_QUERY, placeholders);
        Map<Long, Set<Genre>> result = new HashMap<>();
        jdbc.query(sql, rs -> {
            Long filmId = rs.getLong("film_id");
            Genre genre = new Genre();
            genre.setId(rs.getLong("id"));
            genre.setName(rs.getString("name"));
            result.computeIfAbsent(filmId, k -> new LinkedHashSet<>()).add(genre);
        }, filmIds.toArray());

        return result;
    }

    private Map<Long, Set<Long>> loadLikesForFilms(List<Long> filmIds) {
        if (filmIds.isEmpty()) {
            return Map.of();
        }
        String placeholders = String.join(",", Collections.nCopies(filmIds.size(), "?"));
        String sql = String.format(GET_ALL_LIKES_QUERY, placeholders);

        Map<Long, Set<Long>> result = new HashMap<>();
        jdbc.query(sql, rs -> {
            Long filmId = rs.getLong("film_id");
            Long userId = rs.getLong("user_id");
            result.computeIfAbsent(filmId, k -> new HashSet<>()).add(userId);
        }, filmIds.toArray());

        return result;
    }

    private List<Film> setLikesAndGenresForFilms(List<Film> films) {
        List<Long> filmIds = films.stream()
                .map(Film::getId)
                .toList();
        Map<Long, Set<Genre>> genresByFilm = loadGenresForFilms(filmIds);
        Map<Long, Set<Long>> likesByFilm = loadLikesForFilms(filmIds);
        for (Film film : films) {
            film.setGenres(genresByFilm.getOrDefault(film.getId(), new LinkedHashSet<>()));
            film.setLikes(likesByFilm.getOrDefault(film.getId(), new HashSet<>()));
        }
        return films;
    }

    public List<Film> getRecommendations(Long userId) {
        List<Long> similar = jdbc.queryForList(FIND_SIMILAR_USER_QUERY, Long.class, userId);
        if (similar.isEmpty()) {
            return List.of();
        }
        Long similarUserId = similar.getFirst();
        List<Film> films = findMany(RECOMMENDATIONS_QUERY, similarUserId, userId);
        return setLikesAndGenresForFilms(films);
    }


}