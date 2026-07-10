package ru.yandex.practicum.filmorate.model;

import lombok.*;

import java.util.Objects;


@Data
@AllArgsConstructor
public class Friendship {
    private final Long fromUserId;
    private final Long toUserId;
    private FriendshipStatus status;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Friendship that = (Friendship) o;
        return Objects.equals(fromUserId, that.fromUserId) && Objects.equals(toUserId, that.toUserId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fromUserId, toUserId);
    }


}


