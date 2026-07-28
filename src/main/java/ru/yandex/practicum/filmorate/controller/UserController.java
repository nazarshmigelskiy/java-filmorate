package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.RecommendationService;
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
    private final RecommendationService recommendationService;

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
        return storage.getById(id).orElseThrow(() -> new NotFoundException("Пользователь не найден"));
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

    @GetMapping("/{id}/recommendations")
    public Collection<Film> getRecommendations(@PathVariable @Positive Long id) {
        return recommendationService.getRecommendations(id);
    }
}
