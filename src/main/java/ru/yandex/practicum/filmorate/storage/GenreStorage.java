package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface GenreStorage {
    List<Genre> getAll();

    List<Long> findExistingIds(Collection<Long> ids);

    Optional<Genre> getById(Long id);
}
