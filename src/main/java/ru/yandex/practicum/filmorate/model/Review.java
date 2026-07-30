package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class Review {
    private Long reviewId;
    @NotBlank(message = "content не должен быть пуст")
    private String content;
    @NotNull(message = "isPositive не должен быть null")
    private Boolean isPositive;
    @NotNull(message = "userId не должен быть null")
    private Long userId;
    @NotNull(message = "filmId не должен быть null")
    private Long filmId;
    private Integer useful;
}
