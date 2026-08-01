package ru.yandex.practicum.filmorate.storage;


import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;
import java.util.Optional;

public interface ReviewStorage {

    Review addReview(Review review);

    Optional<Review> updateReview(Review review);

    void deleteReview(Long id);

    Optional<Review> getReviewById(Long id);

    List<Review> getReviews(Long filmId, Integer count);

    void likeReview(Long reviewId, Long userId);

    void dislikeReview(Long reviewId, Long userId);

    void deleteLike(Long reviewId, Long userId);

    void deleteDislike(Long reviewId, Long userId);
}