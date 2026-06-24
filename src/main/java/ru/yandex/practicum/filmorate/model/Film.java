package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import ru.yandex.practicum.filmorate.annotation.MinReleaseDate;

import java.time.LocalDate;


@Data
public class Film {
    private Long id;
    @NotBlank(message = "Имя не может быть пустым")
    private String name;
    @Size(max = 200, message = "Максимальная длина символов - 200")
    private String description;
    @MinReleaseDate
    private LocalDate releaseDate;
    @Positive(message = "Длительность не может быть отрицательной")
    private Integer duration;
}
