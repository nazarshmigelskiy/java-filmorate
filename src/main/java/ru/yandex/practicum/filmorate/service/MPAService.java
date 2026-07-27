package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.storage.MPAStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MPAService {
    private final MPAStorage storage;

    public List<MPA> getAll() {
        return storage.getAll();
    }

    public MPA getById(Long id) {
        return storage.getById(id)
                .orElseThrow(() -> new NotFoundException("Рейтинг с id " + id + " не найден"));
    }
}
