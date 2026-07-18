package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import java.util.List;


@Slf4j
@RequiredArgsConstructor
@Service
public class UserService {
    private final UserStorage storage;

    public User addFriend(Long userId, Long friendId) {
        User user = getUserOrThrow(userId);
        getUserOrThrow(friendId);
        storage.addFriend(userId, friendId);
        log.info("Пользователь {} добавил в друзья {}", userId, friendId);
        return user;
    }

    public User removeFriend(Long userId, Long friendId) {
        User user = getUserOrThrow(userId);
        getUserOrThrow(friendId);
        storage.removeFriend(userId, friendId);
        log.info("Пользователь {} удалил из друзей {}", userId, friendId);
        return user;
    }

    public List<User> getUserFriendList(Long id) {
        getUserOrThrow(id);
        return storage.getFriends(id);
    }

    public List<User> getMutualFriends(Long userId, Long friendId) {
        getUserOrThrow(userId);
        getUserOrThrow(friendId);
        return storage.getMutualFriends(userId, friendId);
    }

    private User getUserOrThrow(Long id) {
        return storage.getById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + id + " не найден"));
    }
}
