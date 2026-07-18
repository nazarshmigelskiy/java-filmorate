package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.MPA;

import java.util.List;
import java.util.Optional;

public interface MPAStorage {
    List<MPA> getAll();

    Optional<MPA> getById(Long id);
}
