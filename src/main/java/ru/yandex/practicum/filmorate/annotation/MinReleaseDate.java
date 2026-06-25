package ru.yandex.practicum.filmorate.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ReleaseDateValidator.class)
public @interface MinReleaseDate {
    String minDate() default "";

    String message() default "Дата релиза не может быть раньше {minDate}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
