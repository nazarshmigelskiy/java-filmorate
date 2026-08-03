package ru.yandex.practicum.filmorate.storage;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Event;

import java.util.Collection;

@Repository
public class EventDbStorage extends BaseStorage<Event> implements EventStorage {
    public EventDbStorage(JdbcTemplate jdbc, RowMapper<Event> mapper) {
        super(jdbc, mapper);
    }

    private static final String INSERT_QUERY = "INSERT INTO events " +
            "(user_id, event_type, operation, created_at, entity_id) " +
            "VALUES (?, ?, ?, ?, ?)";
    private static final String GET_BY_USER_ID_QUERY = "SELECT * FROM events WHERE user_id = ? ORDER BY created_at";

    @Override
    public Event createEvent(Event event) {
        Long id = insert(INSERT_QUERY,
                event.getUserId(),
                event.getEventType().name(),
                event.getOperation().name(),
                event.getTimestamp(),
                event.getEntityId());
        event.setEventId(id);
        return event;
    }

    @Override
    public Collection<Event> getEvents(Long id) {
        return findMany(GET_BY_USER_ID_QUERY, id);
    }
}
