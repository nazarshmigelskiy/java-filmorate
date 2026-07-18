package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.dal.UserRowMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserDbStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Import({UserDbStorage.class, UserRowMapper.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class UserDbStorageTest {

    @Autowired
    private UserDbStorage userStorage;

    private User makeUser() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testlogin");
        user.setName("Тест");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return user;
    }

    @Test
    @DisplayName("Создание и получение пользователя по id")
    void createAndGetById() {
        User created = userStorage.create(makeUser());

        Optional<User> found = userStorage.getById(created.getId());

        assertThat(found)
                .isPresent()
                .hasValueSatisfying(user -> {
                    assertThat(user.getId()).isEqualTo(created.getId());
                    assertThat(user.getEmail()).isEqualTo("test@example.com");
                    assertThat(user.getLogin()).isEqualTo("testlogin");
                });
    }

    @Test
    @DisplayName("Получение несуществующего пользователя")
    void getById_notFound() {
        assertThat(userStorage.getById(999L)).isEmpty();
    }

    @Test
    @DisplayName("Обновление пользователя")
    void updateUser() {
        User created = userStorage.create(makeUser());
        created.setName("Новое имя");

        userStorage.update(created);

        assertThat(userStorage.getById(created.getId()))
                .isPresent()
                .hasValueSatisfying(u -> assertThat(u.getName()).isEqualTo("Новое имя"));
    }

    @Test
    @DisplayName("Получение всех пользователей")
    void getAllUsers() {
        userStorage.create(makeUser());

        Collection<User> users = userStorage.getUsers();

        assertThat(users).isNotEmpty();
    }

    private User makeUser(String email, String login) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName("Тест");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return user;
    }

    @Test
    @DisplayName("Добавление в друзья односторонее")
    void addFriend_oneDirectional() {
        User user1 = userStorage.create(makeUser("u1@example.com", "user1"));
        User user2 = userStorage.create(makeUser("u2@example.com", "user2"));

        userStorage.addFriend(user1.getId(), user2.getId());

        assertThat(userStorage.getFriends(user1.getId()))
                .extracting(User::getId)
                .containsExactly(user2.getId());

        assertThat(userStorage.getFriends(user2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Удаление из друзей")
    void removeFriend() {
        User user1 = userStorage.create(makeUser("u1@example.com", "user1"));
        User user2 = userStorage.create(makeUser("u2@example.com", "user2"));
        userStorage.addFriend(user1.getId(), user2.getId());

        userStorage.removeFriend(user1.getId(), user2.getId());

        assertThat(userStorage.getFriends(user1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Общие друзья")
    void getMutualFriends() {
        User user1 = userStorage.create(makeUser("u1@example.com", "user1"));
        User user2 = userStorage.create(makeUser("u2@example.com", "user2"));
        User common = userStorage.create(makeUser("common@example.com", "common"));

        userStorage.addFriend(user1.getId(), common.getId());
        userStorage.addFriend(user2.getId(), common.getId());

        assertThat(userStorage.getMutualFriends(user1.getId(), user2.getId()))
                .extracting(User::getId)
                .containsExactly(common.getId());
    }

    @Test
    @DisplayName("Общие друзья — нет общих")
    void getMutualFriends_empty() {
        User user1 = userStorage.create(makeUser("u1@example.com", "user1"));
        User user2 = userStorage.create(makeUser("u2@example.com", "user2"));

        assertThat(userStorage.getMutualFriends(user1.getId(), user2.getId())).isEmpty();
    }
}