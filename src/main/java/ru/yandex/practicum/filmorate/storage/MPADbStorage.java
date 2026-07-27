package ru.yandex.practicum.filmorate.storage;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.MPA;

import java.util.List;
import java.util.Optional;

@Repository
public class MPADbStorage extends BaseStorage<MPA> implements MPAStorage {
    private static final String FIND_ALL_QUERY = "SELECT * FROM mpa ORDER BY id";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM mpa WHERE id = ?";

    public MPADbStorage(JdbcTemplate jdbcTemplate, RowMapper<MPA> mapper) {
        super(jdbcTemplate, mapper);
    }

    @Override
    public List<MPA> getAll() {
        return findMany(FIND_ALL_QUERY);
    }

    @Override
    public Optional<MPA> getById(Long id) {
        return findOne(FIND_BY_ID_QUERY, id);
    }
}
