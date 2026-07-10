package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Friendship;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
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
                .filter(friendship -> friendship.getStatus() == FriendshipStatus.CONFIRMED)
                .map(Friendship::getToUserId)
                .map(storage::getById)
                .toList();
    }

    public List<User> getSentUserFriendRequestList(Long id) {
        return storage.getById(id).getFriendsList().stream()
                .filter(friendship -> friendship.getStatus() == FriendshipStatus.PENDING)
                .map(Friendship::getToUserId)
                .map(storage::getById)
                .toList();
    }

    public List<User> getUserFriendRequestList(Long id) {
        return storage.getUsers().stream()
                .flatMap(user -> user.getFriendsList().stream()
                        .filter(f -> f.getToUserId().equals(id))
                        .filter(f -> f.getStatus() == FriendshipStatus.PENDING)
                        .map(f -> user))
                .toList();
    }

    public User acceptFriendship(Long userId, Long friendId) {
        User user = storage.getById(userId);
        boolean hasIncomingRequest = getUserFriendRequestList(userId).stream()
                .anyMatch(u -> u.getId().equals(friendId));
        if (!hasIncomingRequest) {
            throw new ConditionsNotMetException("Пользователь не отправлял вам заявку в друзья");
        }
        getFriendshipById(friendId, userId).setStatus(FriendshipStatus.CONFIRMED);
        user.getFriendsList().add(new Friendship(userId, friendId, FriendshipStatus.CONFIRMED));
        log.info("Пользователи с id {} и {} теперь друзья", userId, friendId);
        return user;
    }

    public User addFriend(Long userId, Long friendId) {
        User user = storage.getById(userId);
        storage.getById(friendId);
        Friendship request = new Friendship(userId, friendId, FriendshipStatus.PENDING);
        boolean alreadyExists = user.getFriendsList().stream()
                .anyMatch(f -> friendId.equals(f.getToUserId()));
        if (alreadyExists)
            throw new ConditionsNotMetException("Запрос уже отправлен или пользователь уже в друзьях.");
        boolean hasIncomingRequest = getUserFriendRequestList(userId).stream()
                .anyMatch(u -> u.getId().equals(friendId));
        if (hasIncomingRequest) {
            acceptFriendship(userId, friendId);
            log.info("Пользователи с id {} и {} теперь друзья", userId, friendId);
        } else {
            user.getFriendsList().add(request);
        }
        return user;
    }

    public User removeFriend(Long userId, Long friendId) {
        User user = storage.getById(userId);
        User friend = storage.getById(friendId);
        Friendship friendship = getFriendshipById(userId, friendId);
        boolean isPending = friendship.getStatus() == FriendshipStatus.PENDING;

        user.getFriendsList().remove(friendship);
        if (!isPending) {
            friend.getFriendsList().remove(getFriendshipById(friendId, userId));
            log.info("Пользователь с id {} удален из друзей пользователя с id {}", friendId, userId);
            log.info("Пользователь с id {} удален из друзей пользователя с id {}", userId, friendId);
        } else {
            log.info("Заявка в друзья к пользователю {} от пользователя с id {} отменена", friendId, userId);
        }
        return user;
    }

    public List<User> getMutualFriends(Long userId, Long friendId) {
        User user = storage.getById(userId);
        User friend = storage.getById(friendId);
        Set<Long> mutualFriends = new HashSet<>(getFriendsId(user));

        mutualFriends.retainAll(getFriendsId(friend));
        return mutualFriends.stream()
                .map(storage::getById)
                .toList();
    }

    private Friendship getFriendshipById(Long userId, Long friendId) {
        return storage.getById(userId).getFriendsList().stream()
                .filter(friendship -> friendId.equals(friendship.getToUserId()))
                .findAny()
                .orElseThrow(() ->
                        new NotFoundException
                                ("Связь между пользователями " + userId + " и " + friendId + " не найдена"));
    }

    private List<Long> getFriendsId(User user) {
        return user.getFriendsList().stream()
                .filter(friendship -> friendship.getStatus() == FriendshipStatus.CONFIRMED)
                .map(Friendship::getToUserId)
                .toList();
    }
}
