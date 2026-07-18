package ru.yandex.practicum.filmorate.storage;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> userList = new HashMap<>();

    @Override
    public Collection<User> getUsers() {
        return userList.values();
    }

    @Override
    public User create(User user) {
        user.setId(getNextId(userList));
        if (user.getName() == null || user.getName().isBlank()) user.setName(user.getLogin());
        userList.put(user.getId(), user);
        log.info("Создан пользователь с id= {}", user.getId());
        return user;
    }

    @Override
    public User update(User user) {
        if (user.getId() == null) {
            throw new ValidationException("Id должен быть указан");
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

    @Override
    public User getById(Long id) {
        if (!userList.containsKey(id)) throw new NotFoundException("Пользователь с указанным id не найден");
        return userList.get(id);
    }

    private long getNextId(Map<Long, User> userList) {
        long currentMaxId = userList.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
