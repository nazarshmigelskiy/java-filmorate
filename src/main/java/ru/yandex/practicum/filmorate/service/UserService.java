package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserService {
    private final UserStorage storage;

    public List<User> getUserFriendList(Long id) {
        return storage.getById(id).getFriendsList().stream()
                .map(storage::getById)
                .toList();
    }

    public User addFriend(Long userId, Long friendId) {
        User user = storage.getById(userId);
        User friend = storage.getById(friendId);
        if (user.getFriendsList().contains(friendId))
            throw new ConditionsNotMetException("Пользователь уже в списке друзей.");
        user.getFriendsList().add(friendId);
        log.info("Пользователь с id {} добавлен в друзья к пользователю с id {}", friendId, userId);
        friend.getFriendsList().add(userId);
        log.info("\"Пользователь с id {} добавлен в друзья к пользователю с id {}", userId, friendId);
        return user;
    }

    public User removeFriend(Long userId, Long friendId) {
        User user = storage.getById(userId);
        User friend = storage.getById(friendId);
        user.getFriendsList().remove(friendId);
        log.info("Пользователь с id {} удален из друзей пользователя с id {}", friendId, userId);
        friend.getFriendsList().remove(userId);
        log.info("Пользователь с id {} удален из друзей пользователя с id {}", userId, friendId);
        return user;
    }

    public List<User> getMutualFriends(Long userId, Long friendId) {
        User user = storage.getById(userId);
        User friend = storage.getById(friendId);
        Set<Long> mutualFriends = new HashSet<>(user.getFriendsList());
        mutualFriends.retainAll(friend.getFriendsList());
        return mutualFriends.stream()
                .map(storage::getById)
                .toList();
    }

}
