package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Operation;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewStorage reviewStorage;
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;
    private final EventService eventService;

    public Review addReview(Review review) {
        if (review.getFilmId() == null || review.getUserId() == null) {
            throw new ValidationException("Идентификатор фильма или пользователя = null");
        }
        validate(review);
        Review created = reviewStorage.addReview(review);
        eventService.addEvent(created.getUserId(), EventType.REVIEW, Operation.ADD, created.getReviewId());
        return created;
    }

    public Review updateReview(Review review) {
        if (review.getReviewId() == null) {
            throw new ConditionsNotMetException("reviewId не может быть null");
        }
        validate(review);
        Review created = reviewStorage.updateReview(review).orElseThrow(() ->
                new NotFoundException("Отзыв не найден"));
        eventService.addEvent(created.getUserId(), EventType.REVIEW, Operation.UPDATE, created.getReviewId());
        return created;
    }

    public void deleteReview(Long id) {
        Review created = getReviewById(id);
        reviewStorage.deleteReview(id);
        eventService.addEvent(created.getUserId(), EventType.REVIEW, Operation.REMOVE, created.getReviewId());
    }

    public Review getReviewById(Long id) {
        return reviewStorage.getReviewById(id).orElseThrow(() -> new NotFoundException("Отзыв не найден"));
    }

    public List<Review> getReviews(Long filmId, Integer count) {
        return reviewStorage.getReviews(filmId, count);
    }

    public void likeReview(Long reviewId, Long userId) {
        getUserByIdOrThrow(userId);
        getReviewById(reviewId);
        reviewStorage.likeReview(reviewId, userId);
    }

    public void dislikeReview(Long reviewId, Long userId) {
        getUserByIdOrThrow(userId);
        reviewStorage.dislikeReview(reviewId, userId);
    }

    public void deleteLike(Long reviewId, Long userId) {
        getUserByIdOrThrow(userId);
        reviewStorage.deleteLike(reviewId, userId);
    }

    public void deleteDislike(Long reviewId, Long userId) {
        getUserByIdOrThrow(userId);
        reviewStorage.deleteDislike(reviewId, userId);
    }

    private void validate(Review review) {
        Long userId = review.getUserId();
        Long filmId = review.getFilmId();
        getUserByIdOrThrow(userId);
        filmStorage.getById(filmId).orElseThrow(() ->
                new NotFoundException("Фильм с id " + filmId + " не найден"));
    }

    private void getUserByIdOrThrow(Long userId) {
        userStorage.getById(userId).orElseThrow(() ->
                new NotFoundException("Пользователь с id " + userId + " не найден"));
    }
}
