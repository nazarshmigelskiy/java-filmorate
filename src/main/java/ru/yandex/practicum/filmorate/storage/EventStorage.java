package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Event;

import java.util.Collection;

public interface EventStorage {

    Event createEvent(Event event);

    Collection<Event> getEvents(Long id);
}
