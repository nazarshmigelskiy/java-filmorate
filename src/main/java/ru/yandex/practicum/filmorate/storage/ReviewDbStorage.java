package ru.yandex.practicum.filmorate.storage;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;
import java.util.Optional;

@Repository
public class ReviewDbStorage extends BaseStorage<Review> implements ReviewStorage {

    // Вставка отзыва (rating не указываем, БД сама ставит 0)
    private static final String INSERT_REVIEW =
            "INSERT INTO Review (content, is_positive, user_id, film_id) " +
                    "VALUES (?, ?, ?, ?)";

    // Обновление отзыва
    private static final String UPDATE_REVIEW =
            "UPDATE Review SET content = ?, is_positive = ? WHERE id = ?";

    // Удаление отзыва
    private static final String DELETE_REVIEW =
            "DELETE FROM Review WHERE id = ?";

    // Получение отзыва по id
    private static final String SELECT_REVIEW_BY_ID =
            "SELECT id, content, is_positive, user_id, film_id, rating FROM Review WHERE id = ?";

    // Отзывы по фильму
    private static final String SELECT_REVIEWS_BY_FILM =
            "SELECT id, content, is_positive, user_id, film_id, rating " +
                    "FROM Review WHERE film_id = ? ORDER BY rating DESC LIMIT ?";

    // Все отзывы
    private static final String SELECT_ALL_REVIEWS =
            "SELECT id, content, is_positive, user_id, film_id, rating " +
                    "FROM Review ORDER BY rating DESC LIMIT ?";

    // Проверка голоса
    private static final String SELECT_VOTE =
            "SELECT is_like FROM ReviewVote WHERE user_id = ? AND review_id = ?";

    // Обновление рейтинга
    private static final String INC_RATING = "UPDATE Review SET rating = rating + 1 WHERE id = ?";
    private static final String DEC_RATING = "UPDATE Review SET rating = rating - 1 WHERE id = ?";
    private static final String INC_RATING_BY_2 = "UPDATE Review SET rating = rating + 2 WHERE id = ?";
    private static final String DEC_RATING_BY_2 = "UPDATE Review SET rating = rating - 2 WHERE id = ?";

    // Вставка голоса
    private static final String INSERT_LIKE =
            "INSERT INTO ReviewVote (user_id, review_id, is_like) VALUES (?, ?, TRUE)";
    private static final String INSERT_DISLIKE =
            "INSERT INTO ReviewVote (user_id, review_id, is_like) VALUES (?, ?, FALSE)";

    // Обновление голоса
    private static final String UPDATE_TO_LIKE =
            "UPDATE ReviewVote SET is_like = TRUE WHERE user_id = ? AND review_id = ?";
    private static final String UPDATE_TO_DISLIKE =
            "UPDATE ReviewVote SET is_like = FALSE WHERE user_id = ? AND review_id = ?";

    // Удаление голоса
    private static final String DELETE_LIKE =
            "DELETE FROM ReviewVote WHERE user_id = ? AND review_id = ? AND is_like = TRUE";
    private static final String DELETE_DISLIKE =
            "DELETE FROM ReviewVote WHERE user_id = ? AND review_id = ? AND is_like = FALSE";

    private static final String DELETE_VOTE =
            "DELETE FROM ReviewVote WHERE user_id = ? AND review_id = ?";

    public ReviewDbStorage(JdbcTemplate jdbc, RowMapper<Review> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public Review addReview(Review review) {
        long id = insert(INSERT_REVIEW,
                review.getContent(),
                review.getIsPositive(),
                review.getUserId(),
                review.getFilmId());
        review.setReviewId(id);
        // rating уже 0 из БД, но устанавливаем явно для уверенности
        review.setUseful(0);
        return review;
    }

    @Override
    public Optional<Review> updateReview(Review review) {
        update(UPDATE_REVIEW,
                review.getContent(),
                review.getIsPositive(),
                review.getReviewId());
        return getReviewById(review.getReviewId());
    }

    @Override
    public void deleteReview(Long id) {
        delete(DELETE_REVIEW, id);
    }

    @Override
    public Optional<Review> getReviewById(Long id) {
        return findOne(SELECT_REVIEW_BY_ID, id);
    }

    @Override
    public List<Review> getReviews(Long filmId, Integer count) {
        if (count == null) count = 10;
        if (filmId != null) {
            return findMany(SELECT_REVIEWS_BY_FILM, filmId, count);
        } else {
            return findMany(SELECT_ALL_REVIEWS, count);
        }
    }

    private Boolean getVote(Long userId, Long reviewId) {
        try {
            return jdbc.queryForObject(SELECT_VOTE, Boolean.class, userId, reviewId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public void likeReview(Long reviewId, Long userId) {
        Boolean vote = getVote(userId, reviewId);
        if (vote == null) {
            jdbc.update(INSERT_LIKE, userId, reviewId);
            updateRatingWithRollback(reviewId, INC_RATING, DELETE_VOTE, userId, reviewId);

        } else if (!vote) {
            jdbc.update(UPDATE_TO_LIKE, userId, reviewId);
            updateRatingWithRollback(reviewId, INC_RATING_BY_2, UPDATE_TO_DISLIKE, userId, reviewId);
        }
    }

    @Override
    public void dislikeReview(Long reviewId, Long userId) {
        Boolean vote = getVote(userId, reviewId);
        if (vote == null) {
            jdbc.update(INSERT_DISLIKE, userId, reviewId);
            updateRatingWithRollback(reviewId, DEC_RATING, DELETE_VOTE, userId, reviewId);

        } else if (vote) {
            jdbc.update(UPDATE_TO_DISLIKE, userId, reviewId);
            updateRatingWithRollback(reviewId, DEC_RATING_BY_2, UPDATE_TO_LIKE, userId, reviewId);
        }
    }

    @Override
    public void deleteLike(Long reviewId, Long userId) {
        int rows = jdbc.update(DELETE_LIKE, userId, reviewId);
        if (rows > 0) {
            update(DEC_RATING, reviewId);
        }
    }

    @Override
    public void deleteDislike(Long reviewId, Long userId) {
        int rows = jdbc.update(DELETE_DISLIKE, userId, reviewId);
        if (rows > 0) {
            update(INC_RATING, reviewId);
        }
    }

    /**
     * Обновляет рейтинг с возможностью отката
     */
    private void updateRatingWithRollback(Long reviewId, String updateSql,
                                          String rollbackSql, Object... rollbackParams) {
        int updatedRows = jdbc.update(updateSql, reviewId);
        if (updatedRows == 0) {
            jdbc.update(rollbackSql, rollbackParams);
            throw new NotFoundException("Отзыв с id=" + reviewId + " не найден");
        }
    }
}