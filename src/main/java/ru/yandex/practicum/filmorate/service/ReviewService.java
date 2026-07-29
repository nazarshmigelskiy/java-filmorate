package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.ReviewDbStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewDbStorage reviewDbStorage;

    public Review addReview(Review review) {
        validateUserId(review.getUserId());
        validateFilmId(review.getFilmId());
        return reviewDbStorage.addReview(review);
    }

    public Review updateReview(Review review) {
        return reviewDbStorage.updateReview(review);
    }

    public void deleteReview(Long id) {
        validateReviewId(id);
        reviewDbStorage.deleteReview(id);
    }

    public Review getReviewById(Long id) {
        validateReviewId(id);
        return reviewDbStorage.getReviewById(id);
    }

    public List<Review> getReviews(Long filmId, Integer count) {
        validateFilmId(filmId);
        return reviewDbStorage.getReviews(filmId, count);
    }

    public void likeReview(Long reviewId, Long userId) {
        validateReviewId(reviewId);
        validateUserId(userId);
        getReviewById(reviewId);
        reviewDbStorage.likeReview(reviewId, userId);
    }

    public void dislikeReview(Long reviewId, Long userId) {
        validateReviewId(reviewId);
        validateUserId(userId);
        reviewDbStorage.dislikeReview(reviewId, userId);
    }

    public void deleteLike(Long reviewId, Long userId) {
        validateReviewId(reviewId);
        validateUserId(userId);
        reviewDbStorage.deleteLike(reviewId, userId);
    }

    public void deleteDislike(Long reviewId, Long userId) {
        validateReviewId(reviewId);
        validateUserId(userId);
        reviewDbStorage.deleteDislike(reviewId, userId);
    }

    private void validateUserId(Long userId) {
        if (userId <= 0) {
            throw new NotFoundException("userId не может быть меньше или равен 0");
        }
    }

    private void validateFilmId(Long filmId) {
        if (filmId <= 0) {
            throw new NotFoundException("filmId не может быть меньше или равен 0");
        }
    }

    private void validateReviewId(Long reviewId) {
        if (reviewId <= 0) {
            throw new NotFoundException("reviewId не может быть меньше или равен 0");
        }
    }
}
