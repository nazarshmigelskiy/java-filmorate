package ru.yandex.practicum.filmorate.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum RatesMPA {
    G(1,"G"),
    PG(2,"PG"),
    PG13(3, "PG-13"),
    R(4,"R"),
    NC17(5,"NC-17");

    private final int id;
    private final String name;
}
