package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.controller.ReviewController;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ReviewControllerTest {

    @Autowired
    private ReviewController reviewController;

    @Autowired
    private UserController userController;

    @Autowired
    private FilmController filmController;

    private User testUser;
    private User testUser2;
    private Film testFilm;
    private Review testReview;

    @BeforeEach
    void setUp() {
        testUser = createTestUser();
        testUser = userController.create(testUser);

        testUser2 = createTestUser();
        testUser2 = userController.create(testUser2);

        testFilm = createTestFilm();
        testFilm = filmController.addFilm(testFilm);

        testReview = createTestReview();
    }

    private User createTestUser() {
        User user = new User();
        user.setEmail("user_" + UUID.randomUUID() + "@test.ru");
        user.setLogin("login_" + UUID.randomUUID());
        user.setName("User_" + UUID.randomUUID());
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return user;
    }

    private Film createTestFilm() {
        Film film = new Film();
        film.setName("Film_" + UUID.randomUUID());
        film.setDescription("Description_" + UUID.randomUUID());
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        MPA mpa = new MPA();
        mpa.setId(1L);
        mpa.setName("G");
        film.setMpa(mpa);
        return film;
    }

    private Review createTestReview() {
        Review review = new Review();
        review.setContent("Test review content " + UUID.randomUUID());
        review.setIsPositive(true);
        review.setUserId(testUser.getId());
        review.setFilmId(testFilm.getId());
        return review;
    }

    @Test
    void createReviewWithValidDataShouldSetIdAndReturnReview() {
        Review created = reviewController.createReview(testReview);

        assertNotNull(created.getReviewId());
        assertTrue(created.getReviewId() > 0);
        assertEquals(testReview.getContent(), created.getContent());
        assertEquals(testReview.getIsPositive(), created.getIsPositive());
        assertEquals(testReview.getUserId(), created.getUserId());
        assertEquals(testReview.getFilmId(), created.getFilmId());
        assertEquals(0, created.getUseful()); // начальный рейтинг = 0
    }

    @Test
    void createReviewWithNullContentShouldThrow() {
        testReview.setContent(null);
        assertThrows(Exception.class, () -> reviewController.createReview(testReview));
    }

    @Test
    void updateReviewWithValidDataShouldUpdateFields() {
        Review created = reviewController.createReview(testReview);
        created.setContent("Updated content");
        created.setIsPositive(false);

        Review updated = reviewController.updateReview(created);

        assertEquals(created.getReviewId(), updated.getReviewId());
        assertEquals("Updated content", updated.getContent());
        assertFalse(updated.getIsPositive());
    }

    @Test
    void updateReviewWithNullIdShouldThrow() {
        testReview.setReviewId(null);
        assertThrows(ConditionsNotMetException.class, () -> reviewController.updateReview(testReview));
    }

    @Test
    void updateReviewWithNonExistingIdShouldThrow() {
        testReview.setReviewId(999999L);
        assertThrows(NotFoundException.class, () -> reviewController.updateReview(testReview));
    }

    @Test
    void getReviewByIdShouldReturnReview() {
        Review created = reviewController.createReview(testReview);
        Review found = reviewController.getReviewById(created.getReviewId());

        assertEquals(created.getReviewId(), found.getReviewId());
        assertEquals(created.getContent(), found.getContent());
        assertEquals(created.getIsPositive(), found.getIsPositive());
    }

    @Test
    void getReviewByNonExistingIdShouldThrow() {
        assertThrows(NotFoundException.class, () -> reviewController.getReviewById(999999L));
    }

    @Test
    void getReviewsByFilmIdShouldReturnFilmReviews() {
        reviewController.createReview(testReview);

        List<Review> reviews = reviewController.getReviews(testFilm.getId(), 10);

        assertNotNull(reviews);
        assertTrue(reviews.size() >= 1);
        assertEquals(testFilm.getId(), reviews.get(0).getFilmId());
    }

    @Test
    void deleteReviewShouldRemoveReview() {
        Review created = reviewController.createReview(testReview);
        reviewController.deleteReview(created.getReviewId());

        assertThrows(NotFoundException.class, () -> reviewController.getReviewById(created.getReviewId()));
    }

    @Test
    void likeReviewShouldIncreaseUseful() {
        Review created = reviewController.createReview(testReview);
        assertEquals(0, created.getUseful());

        reviewController.likeReview(created.getReviewId(), testUser2.getId());

        Review updated = reviewController.getReviewById(created.getReviewId());
        assertEquals(1, updated.getUseful());
    }

    @Test
    void dislikeReviewShouldDecreaseUseful() {
        Review created = reviewController.createReview(testReview);
        assertEquals(0, created.getUseful());

        reviewController.dislikeReview(created.getReviewId(), testUser2.getId());

        Review updated = reviewController.getReviewById(created.getReviewId());
        assertEquals(-1, updated.getUseful());
    }

    @Test
    void likeReviewWithNonExistingReviewShouldThrow() {
        assertThrows(NotFoundException.class, () -> reviewController.likeReview(999999L, testUser2.getId()));
    }

    @Test
    void deleteLikeShouldDecreaseUseful() {
        Review created = reviewController.createReview(testReview);
        reviewController.likeReview(created.getReviewId(), testUser2.getId());

        Review afterLike = reviewController.getReviewById(created.getReviewId());
        assertEquals(1, afterLike.getUseful());

        reviewController.deleteLike(created.getReviewId(), testUser2.getId());

        Review afterDelete = reviewController.getReviewById(created.getReviewId());
        assertEquals(0, afterDelete.getUseful());
    }

    @Test
    void deleteDislikeShouldIncreaseUseful() {
        Review created = reviewController.createReview(testReview);
        reviewController.dislikeReview(created.getReviewId(), testUser2.getId());

        Review afterDislike = reviewController.getReviewById(created.getReviewId());
        assertEquals(-1, afterDislike.getUseful());

        reviewController.deleteDislike(created.getReviewId(), testUser2.getId());

        Review afterDelete = reviewController.getReviewById(created.getReviewId());
        assertEquals(0, afterDelete.getUseful());
    }

    @Test
    void changeLikeToDislikeShouldUpdateUsefulCorrectly() {
        Review created = reviewController.createReview(testReview);

        reviewController.likeReview(created.getReviewId(), testUser2.getId());
        Review afterLike = reviewController.getReviewById(created.getReviewId());
        assertEquals(1, afterLike.getUseful());

        reviewController.dislikeReview(created.getReviewId(), testUser2.getId());
        Review afterDislike = reviewController.getReviewById(created.getReviewId());
        assertEquals(-1, afterDislike.getUseful());
    }

    @Test
    void changeDislikeToLikeShouldUpdateUsefulCorrectly() {
        Review created = reviewController.createReview(testReview);

        reviewController.dislikeReview(created.getReviewId(), testUser2.getId());
        Review afterDislike = reviewController.getReviewById(created.getReviewId());
        assertEquals(-1, afterDislike.getUseful());

        reviewController.likeReview(created.getReviewId(), testUser2.getId());
        Review afterLike = reviewController.getReviewById(created.getReviewId());
        assertEquals(1, afterLike.getUseful());
    }

    @Test
    void multipleUsersCanLikeSameReview() {
        Review created = reviewController.createReview(testReview);

        User user3 = createTestUser();
        user3 = userController.create(user3);

        reviewController.likeReview(created.getReviewId(), testUser2.getId());
        reviewController.likeReview(created.getReviewId(), user3.getId());

        Review updated = reviewController.getReviewById(created.getReviewId());
        assertEquals(2, updated.getUseful());
    }

    @Test
    void sameUserCannotLikeTwice() {
        Review created = reviewController.createReview(testReview);

        reviewController.likeReview(created.getReviewId(), testUser2.getId());
        reviewController.likeReview(created.getReviewId(), testUser2.getId()); // второй раз

        Review updated = reviewController.getReviewById(created.getReviewId());
        assertEquals(1, updated.getUseful()); // не увеличилось
    }

    @Test
    void reviewsShouldBeSortedByUsefulDescending() {
        Review review1 = createTestReview();
        review1.setContent("Review 1");
        Review created1 = reviewController.createReview(review1);

        Review review2 = createTestReview();
        review2.setContent("Review 2");
        Review created2 = reviewController.createReview(review2);

        User user3 = createTestUser();
        user3 = userController.create(user3);

        reviewController.likeReview(created2.getReviewId(), testUser2.getId());
        reviewController.likeReview(created2.getReviewId(), user3.getId());

        List<Review> reviews = reviewController.getReviews(testFilm.getId(), 10);

        assertTrue(reviews.size() >= 2);

        assertEquals("Review 2", reviews.get(0).getContent());
        assertEquals(2, reviews.get(0).getUseful());
    }
}