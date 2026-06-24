package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.annotation.ReleaseDateValidator;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ReleaseDateValidatorTest {

    private ReleaseDateValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ReleaseDateValidator();
    }

    @Test
    @DisplayName("null")
    void isValid_null_returnsTrue() {
        assertTrue(validator.isValid(null, null));
    }

    @Test
    @DisplayName("Ровно 28 декабря 1895")
    void isValid_exactMinDate_returnsTrue() {
        assertTrue(validator.isValid(LocalDate.of(1895, 12, 28), null));
    }

    @Test
    @DisplayName("27 декабря 1895")
    void isValid_oneDayBeforeMinDate_returnsFalse() {
        assertFalse(validator.isValid(LocalDate.of(1895, 12, 27), null));
    }

    @Test
    @DisplayName("1 января 1895")
    void isValid_wayBeforeMinDate_returnsFalse() {
        assertFalse(validator.isValid(LocalDate.of(1895, 1, 1), null));
    }

    @Test
    @DisplayName("29 декабря 1895")
    void isValid_oneDayAfterMinDate_returnsTrue() {
        assertTrue(validator.isValid(LocalDate.of(1895, 12, 29), null));
    }

    @Test
    @DisplayName("Современная дата")
    void isValid_modernDate_returnsTrue() {
        assertTrue(validator.isValid(LocalDate.of(2024, 6, 15), null));
    }

    @Test
    @DisplayName("Будущая дата")
    void isValid_futureDate_returnsTrue() {
        assertTrue(validator.isValid(LocalDate.of(2099, 1, 1), null));
    }
}