package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.List;
import java.util.Optional;


public interface UserStorage {
    Collection<User> getUsers();

    User create(User user);

    User update(User user);

    Optional<User> getById(Long id);

    void addFriend(Long userId, Long friendId);

    void removeFriend(Long userId, Long friendId);

    List<User> getFriends(Long userId);

    List<User> getMutualFriends(Long userId, Long friendId);
}
