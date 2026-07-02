package ru.yandex.practicum.filmorate.annotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;

public class ReleaseDateValidator implements ConstraintValidator<MinReleaseDate, LocalDate> {

    private LocalDate minDate;

    @Override
    public void initialize(MinReleaseDate annotation) {
        this.minDate = LocalDate.parse(annotation.minDate());
    }

    @Override
    public boolean isValid(LocalDate date, ConstraintValidatorContext context) {
        if (date == null) return true;
        return !date.isBefore(minDate);
    }
}