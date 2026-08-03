package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class DirectorService {
    private final DirectorStorage directorStorage;

    public Collection<Director> getAll() {
        return directorStorage.getAll();
    }

    public Director getById(Long id) {
        return directorStorage.getById(id).orElseThrow(() -> new NotFoundException("Режиссер с id " + id + " не найден"));
    }

    public Director addDirector(Director director) {
        return directorStorage.create(director);
    }

    public Director updateDirector(Director director) {
        return directorStorage.update(director);
    }

    public void deleteDirector(Long id) {
        directorStorage.getById(id).orElseThrow(() -> new NotFoundException("Режиссер с id " + id + " не найден"));
        directorStorage.delete(id);
    }
}
