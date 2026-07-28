package ru.yandex.practicum.filmorate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserStorage storage;

    @MockitoBean
    private UserService service;

    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
    }

    private User validUser() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setLogin("testlogin");
        user.setName("Тестовый пользователь");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return user;
    }

    // ─── POST /users ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("Создание валидного пользователя")
    void createUser_valid() throws Exception {
        User user = validUser();
        when(storage.create(any(User.class))).thenReturn(user);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.login").value("testlogin"));
    }

    @Test
    @DisplayName("Email без @")
    void createUser_invalidEmail() throws Exception {
        User user = validUser();
        user.setEmail("notanemail");
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Пустой email")
    void createUser_emptyEmail() throws Exception {
        User user = validUser();
        user.setEmail("");
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Пустой логин")
    void createUser_blankLogin() throws Exception {
        User user = validUser();
        user.setLogin("   ");
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Логин с пробелом в середине")
    void createUser_loginWithSpace() throws Exception {
        User user = validUser();
        user.setLogin("login with space");
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Логин с пробелом в начале")
    void createUser_loginWithLeadingSpace() throws Exception {
        User user = validUser();
        user.setLogin(" login");
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Пустое имя — подстановка логина сервисом хранения")
    void createUser_emptyName_delegatesToStorage() throws Exception {
        User user = validUser();
        user.setName("");
        User stored = validUser();
        stored.setName(stored.getLogin());
        when(storage.create(any(User.class))).thenReturn(stored);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(user.getLogin()));
    }

    @Test
    @DisplayName("Дата рождения сегодня — граница")
    void createUser_birthdayToday() throws Exception {
        User user = validUser();
        user.setBirthday(LocalDate.now());
        when(storage.create(any(User.class))).thenReturn(user);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(user)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Дата рождения завтра — граница+1")
    void createUser_birthdayInFuture() throws Exception {
        User user = validUser();
        user.setBirthday(LocalDate.now().plusDays(1));
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
    }

    // ─── PUT /users ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Обновление несуществующего пользователя")
    void updateUser_notFound() throws Exception {
        User user = validUser();
        user.setId(999L);
        when(storage.update(any(User.class))).thenThrow(new NotFoundException("Пользователь не найден"));

        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(user)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Обновление существующего пользователя")
    void updateUser_success() throws Exception {
        User user = validUser();
        user.setEmail("new@example.com");
        when(storage.update(any(User.class))).thenReturn(user);

        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("new@example.com"));
    }

    // ─── GET /users ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Список пользователей")
    void getUsers_success() throws Exception {
        when(storage.getUsers()).thenReturn(List.of(validUser()));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("Получение пользователя по id")
    void getUserById_success() throws Exception {
        when(storage.getById(1L)).thenReturn(Optional.of(validUser()));

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("Получение пользователя по несуществующему id")
    void getUserById_notFound() throws Exception {
        when(storage.getById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/users/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Некорректный id — отрицательный")
    void getUserById_negativeId() throws Exception {
        mockMvc.perform(get("/users/-1"))
                .andExpect(status().isBadRequest());
    }

    // ─── PUT /users/{id}/friends/{friendId} ───────────────────────────────────

    @Test
    @DisplayName("Добавление в друзья")
    void addFriend_success() throws Exception {
        User user = validUser();
        when(service.addFriend(1L, 2L)).thenReturn(user);

        mockMvc.perform(put("/users/1/friends/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("Добавление в друзья несуществующего пользователя")
    void addFriend_userNotFound() throws Exception {
        when(service.addFriend(999L, 1L)).thenThrow(new NotFoundException("Пользователь с id 999 не найден"));

        mockMvc.perform(put("/users/999/friends/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Добавление в друзья несуществующего друга")
    void addFriend_friendNotFound() throws Exception {
        when(service.addFriend(1L, 999L)).thenThrow(new NotFoundException("Пользователь с id 999 не найден"));

        mockMvc.perform(put("/users/1/friends/999"))
                .andExpect(status().isNotFound());
    }

    // ─── DELETE /users/{id}/friends/{friendId} ─────────────────────────────────

    @Test
    @DisplayName("Удаление из друзей")
    void removeFriend_success() throws Exception {
        User user = validUser();
        when(service.removeFriend(1L, 2L)).thenReturn(user);

        mockMvc.perform(delete("/users/1/friends/2"))
                .andExpect(status().isOk());
        verify(service).removeFriend(1L, 2L);
    }

    @Test
    @DisplayName("Удаление из друзей несуществующего пользователя")
    void removeFriend_notFound() throws Exception {
        when(service.removeFriend(999L, 1L)).thenThrow(new NotFoundException("Пользователь с id 999 не найден"));

        mockMvc.perform(delete("/users/999/friends/1"))
                .andExpect(status().isNotFound());
    }

    // ─── GET /users/{id}/friends ───────────────────────────────────────────────

    @Test
    @DisplayName("Список друзей пользователя")
    void getFriends_success() throws Exception {
        User friend = validUser();
        friend.setId(2L);
        when(service.getUserFriendList(1L)).thenReturn(List.of(friend));

        mockMvc.perform(get("/users/1/friends"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(2));
    }

    @Test
    @DisplayName("Список друзей несуществующего пользователя")
    void getFriends_userNotFound() throws Exception {
        when(service.getUserFriendList(999L)).thenThrow(new NotFoundException("Пользователь с id 999 не найден"));

        mockMvc.perform(get("/users/999/friends"))
                .andExpect(status().isNotFound());
    }

    // ─── GET /users/{id}/friends/common/{friendId} ────────────────────────────

    @Test
    @DisplayName("Общие друзья — есть общий")
    void getMutualFriends_hasMutual() throws Exception {
        User common = validUser();
        common.setId(3L);
        when(service.getMutualFriends(1L, 2L)).thenReturn(List.of(common));

        mockMvc.perform(get("/users/1/friends/common/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(3));
    }

    @Test
    @DisplayName("Общие друзья — нет общих")
    void getMutualFriends_noMutual() throws Exception {
        when(service.getMutualFriends(1L, 2L)).thenReturn(List.of());

        mockMvc.perform(get("/users/1/friends/common/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
