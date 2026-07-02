package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final Map<Long, User> userList = new HashMap<>();

    @GetMapping
    public Collection<User> getUsers() {
        return userList.values();
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        user.setId(getNextId());
        if (user.getName() == null || user.getName().isBlank()) user.setName(user.getLogin());
        userList.put(user.getId(), user);
        log.info("Создан пользователь с id= {}", user.getId());
        return user;
    }

    @PutMapping
    public User update(@Valid @RequestBody User user) {
        if (user.getId() == null) {
            throw new ConditionsNotMetException("Id должен быть указан");
        }
        if (userList.containsKey(user.getId())) {
            if (user.getName() == null || user.getName().isBlank()) user.setName(user.getLogin());
            userList.put(user.getId(), user);
            log.info("Информация о пользователе с id= {} обновлена", user.getId());
            return user;
        } else {
            throw new NotFoundException("Пользователь не найден");
        }
    }

    private long getNextId() {
        long currentMaxId = userList.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
