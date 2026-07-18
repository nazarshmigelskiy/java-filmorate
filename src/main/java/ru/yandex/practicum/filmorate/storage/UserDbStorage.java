package ru.yandex.practicum.filmorate.storage;

import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Optional;


@Repository
@Primary
public class UserDbStorage extends BaseStorage<User> implements UserStorage {
    private static final String FIND_ALL_QUERY =
            "SELECT * FROM users";
    private static final String FIND_BY_ID_QUERY =
            "SELECT * FROM users WHERE id = ?";
    private static final String INSERT_QUERY =
            "INSERT INTO users(email, login, name, birthday) VALUES(?, ?, ?, ?)";
    private static final String UPDATE_QUERY =
            "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?";
    private static final String ADD_FRIEND_QUERY =
            "INSERT INTO friendships (user_id, friend_id) VALUES (?, ?)";
    private static final String REMOVE_FRIEND_QUERY =
            "DELETE FROM friendships WHERE user_id = ? AND friend_id = ?";
    private static final String GET_FRIENDS_QUERY =
            "SELECT u.* FROM users u " +
                    "JOIN friendships f ON u.id = f.friend_id " +
                    "WHERE f.user_id = ?";
    private static final String GET_MUTUAL_FRIENDS_QUERY =
            "SELECT u.* FROM users u " +
                    "JOIN friendships f1 ON u.id = f1.friend_id AND f1.user_id = ? " +
                    "JOIN friendships f2 ON u.id = f2.friend_id AND f2.user_id = ?";

    public UserDbStorage(JdbcTemplate jdbc, RowMapper<User> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public List<User> getUsers() {
        return findMany(FIND_ALL_QUERY);
    }

    @Override
    public Optional<User> getById(Long id) {
        return findOne(FIND_BY_ID_QUERY, id);
    }

    @Override
    public User create(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        long id = insert(
                INSERT_QUERY,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday());
        user.setId(id);
        return user;
    }

    @Override
    public User update(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        update(
                UPDATE_QUERY,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday(),
                user.getId());
        return user;
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        jdbc.update(ADD_FRIEND_QUERY, userId, friendId);
    }

    @Override
    public void removeFriend(Long userId, Long friendId) {
        jdbc.update(REMOVE_FRIEND_QUERY, userId, friendId);
    }

    @Override
    public List<User> getFriends(Long userId) {
        return findMany(GET_FRIENDS_QUERY, userId);
    }

    @Override
    public List<User> getMutualFriends(Long userId, Long friendId) {
        return findMany(GET_MUTUAL_FRIENDS_QUERY, userId, friendId);
    }
}