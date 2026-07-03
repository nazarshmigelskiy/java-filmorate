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
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
    }

    private User validUser() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testlogin");
        user.setName("Тестовый пользователь");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return user;
    }

    private User createUser() throws Exception {
        String response = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(validUser())))
                .andReturn().getResponse().getContentAsString();
        return mapper.readValue(response, User.class);
    }

    private User createUser(String email, String login) throws Exception {
        User user = validUser();
        user.setEmail(email);
        user.setLogin(login);
        String response = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(user)))
                .andReturn().getResponse().getContentAsString();
        return mapper.readValue(response, User.class);
    }


    @Test
    @DisplayName("Создание валидного пользователя")
    void createUser_valid() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(validUser())))
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
    @DisplayName("Пустое имя — подстановка логина")
    void createUser_emptyName_usesLogin() throws Exception {
        User user = validUser();
        user.setName("");
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(user.getLogin()));
    }

    @Test
    @DisplayName("Null имя — подстановка логина")
    void createUser_nullName_usesLogin() throws Exception {
        User user = validUser();
        user.setName(null);
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


    @Test
    @DisplayName("Обновление без id")
    void updateUser_noId() throws Exception {
        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(validUser())))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Обновление несуществующего пользователя")
    void updateUser_notFound() throws Exception {
        User user = validUser();
        user.setId(999L);
        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(user)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Обновление существующего пользователя")
    void updateUser_success() throws Exception {
        User created = createUser();
        created.setEmail("new@example.com");
        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(created)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("new@example.com"));
    }

    @Test
    @DisplayName("Обновление: пустое имя — подстановка логина")
    void updateUser_emptyName_usesLogin() throws Exception {
        User created = createUser();
        created.setName("");
        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(created)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(created.getLogin()));
    }


    @Test
    @DisplayName("Получение пользователя по id")
    void getUserById_success() throws Exception {
        User created = createUser();
        mockMvc.perform(get("/users/" + created.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(created.getId()));
    }

    @Test
    @DisplayName("Получение пользователя по несуществующему id")
    void getUserById_notFound() throws Exception {
        mockMvc.perform(get("/users/999"))
                .andExpect(status().isNotFound());
    }


    @Test
    @DisplayName("Добавление в друзья")
    void addFriend_success() throws Exception {
        User user = createUser("user@example.com", "userlogin");
        User friend = createUser("friend@example.com", "friendlogin");

        mockMvc.perform(put("/users/" + user.getId() + "/friends/" + friend.getId()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Повторное добавление одного и того же друга")
    void addFriend_alreadyFriends() throws Exception {
        User user = createUser("user@example.com", "userlogin");
        User friend = createUser("friend@example.com", "friendlogin");

        mockMvc.perform(put("/users/" + user.getId() + "/friends/" + friend.getId()));
        mockMvc.perform(put("/users/" + user.getId() + "/friends/" + friend.getId()))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("Добавление несуществующего пользователя в друзья")
    void addFriend_userNotFound() throws Exception {
        mockMvc.perform(put("/users/999/friends/1"))
                .andExpect(status().isNotFound());
    }


    @Test
    @DisplayName("Удаление из друзей")
    void removeFriend_success() throws Exception {
        User user = createUser("user@example.com", "userlogin");
        User friend = createUser("friend@example.com", "friendlogin");

        mockMvc.perform(put("/users/" + user.getId() + "/friends/" + friend.getId()));
        mockMvc.perform(delete("/users/" + user.getId() + "/friends/" + friend.getId()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Удаление из друзей того, кто не в списке")
    void removeFriend_notInFriendList() throws Exception {
        User user = createUser("user@example.com", "userlogin");
        User friend = createUser("friend@example.com", "friendlogin");

        mockMvc.perform(delete("/users/" + user.getId() + "/friends/" + friend.getId()))
                .andExpect(status().isOk());
    }


    @Test
    @DisplayName("Общие друзья — есть общий")
    void getMutualFriends_hasMutual() throws Exception {
        User user1 = createUser("user1@example.com", "user1");
        User user2 = createUser("user2@example.com", "user2");
        User common = createUser("common@example.com", "common");

        mockMvc.perform(put("/users/" + user1.getId() + "/friends/" + common.getId()));
        mockMvc.perform(put("/users/" + user2.getId() + "/friends/" + common.getId()));

        mockMvc.perform(get("/users/" + user1.getId() + "/friends/common/" + user2.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(common.getId()));
    }

    @Test
    @DisplayName("Общие друзья — нет общих")
    void getMutualFriends_noMutual() throws Exception {
        User user1 = createUser("user1@example.com", "user1");
        User user2 = createUser("user2@example.com", "user2");

        mockMvc.perform(get("/users/" + user1.getId() + "/friends/common/" + user2.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}