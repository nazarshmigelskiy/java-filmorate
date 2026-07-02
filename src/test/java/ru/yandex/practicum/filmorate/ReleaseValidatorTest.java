package ru.yandex.practicum.filmorate;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.annotation.MinReleaseDate;
import ru.yandex.practicum.filmorate.annotation.ReleaseDateValidator;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReleaseValidatorTest {

    private ReleaseDateValidator createValidator(String minDate) {
        MinReleaseDate annotation = mock(MinReleaseDate.class);
        when(annotation.minDate()).thenReturn(minDate);

        ReleaseDateValidator validator = new ReleaseDateValidator();
        validator.initialize(annotation);
        return validator;
    }

    @Test
    @DisplayName("null")
    void isValid_null_returnsTrue() {
        ReleaseDateValidator validator = createValidator("1895-12-28");
        assertTrue(validator.isValid(null, null));
    }

    @Test
    @DisplayName("Ровно 28 декабря 1895")
    void isValid_exactMinDate_returnsTrue() {
        ReleaseDateValidator validator = createValidator("1895-12-28");
        assertTrue(validator.isValid(LocalDate.of(1895, 12, 28), null));
    }

    @Test
    @DisplayName("27 декабря 1895")
    void isValid_oneDayBeforeMinDate_returnsFalse() {
        ReleaseDateValidator validator = createValidator("1895-12-28");
        assertFalse(validator.isValid(LocalDate.of(1895, 12, 27), null));
    }

    @Test
    @DisplayName("29 декабря 1895")
    void isValid_oneDayAfterMinDate_returnsTrue() {
        ReleaseDateValidator validator = createValidator("1895-12-28");
        assertTrue(validator.isValid(LocalDate.of(1895, 12, 29), null));
    }

    @Test
    @DisplayName("Современная дата")
    void isValid_modernDate_returnsTrue() {
        ReleaseDateValidator validator = createValidator("1895-12-28");
        assertTrue(validator.isValid(LocalDate.of(2024, 6, 15), null));
    }

    @Test
    @DisplayName("Кастомная минимальная дата")
    void isValid_customMinDate_exactBoundary_returnsTrue() {
        ReleaseDateValidator validator = createValidator("1900-01-01");
        assertTrue(validator.isValid(LocalDate.of(1900, 1, 1), null));
    }

    @Test
    @DisplayName("Кастомная минимальная дата — ниже границы")
    void isValid_customMinDate_oneDayBefore_returnsFalse() {
        ReleaseDateValidator validator = createValidator("1900-01-01");
        assertFalse(validator.isValid(LocalDate.of(1899, 12, 31), null));
    }
}