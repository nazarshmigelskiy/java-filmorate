package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;

@Validated
@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserStorage storage;
    private final UserService service;

    @GetMapping
    public Collection<User> getUsers() {
        return storage.getUsers();
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        return storage.create(user);
    }

    @PutMapping
    public User update(@Valid @RequestBody User user) {
        return storage.update(user);
    }

    @GetMapping("/{id}")
    public User getById(@PathVariable @Positive Long id) {
        return storage.getById(id);
    }

    @GetMapping("/{id}/friends")
    public Collection<User> getFriends(@PathVariable @Positive Long id) {
        return service.getUserFriendList(id);
    }

    @GetMapping("/{id}/friends/common/{friendId}")
    public Collection<User> getMutualFriends(@PathVariable @Positive Long id,
                                             @PathVariable @Positive Long friendId) {
        return service.getMutualFriends(id, friendId);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public User addFriend(@PathVariable @Positive Long id,
                          @PathVariable @Positive Long friendId) {
        return service.addFriend(id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public User deleteFriend(@PathVariable @Positive Long id,
                             @PathVariable @Positive Long friendId) {
        return service.removeFriend(id, friendId);
    }

    @PutMapping("/{id}/friends/{friendId}/accept")
    public User acceptFriend(@PathVariable @Positive Long id,
                             @PathVariable @Positive Long friendId) {
        return service.acceptFriendship(id, friendId);
    }

    @GetMapping("/{id}/friends/pending")
    public Collection<User> getFriendshipRequests(@PathVariable @Positive Long id) {
        return service.getUserFriendRequestList(id);
    }

    @GetMapping("/{id}/friends/pending/sent")
    public Collection<User> getSentFriendshipRequests(@PathVariable @Positive Long id) {
        return service.getSentUserFriendRequestList(id);
    }
}
