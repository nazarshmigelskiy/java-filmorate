package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface DirectorStorage {
    List<Director> getAll();

    Optional<Director> getById(Long id);

    Director create(Director director);

    Director update(Director director);

    void delete(Long id);

    List<Long> findExistingIds(Collection<Long> ids);
}
