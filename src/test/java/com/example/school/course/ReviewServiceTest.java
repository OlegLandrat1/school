package com.example.school.course;

import com.example.school.user.ProfileRepository;
import com.example.school.user.Role;
import com.example.school.user.User;
import com.example.school.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class ReviewServiceTest {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private CourseReviewRepository reviews;

    @Autowired
    private CourseRepository courses;

    @Autowired
    private UserRepository users;

    @Autowired
    private ProfileRepository profiles;

    private Long userId;
    private Long courseId;

    @BeforeEach
    void setUp() {
        courses.deleteAll();
        profiles.deleteAll();
        users.deleteAll();

        userId = users.save(new User("anna", "hash", Role.STUDENT)).getId();
        courseId = courses.save(new Course("Java")).getId();
    }

    @Test
    void addsReview() {
        reviewService.addReview(userId, courseId, 5, "Great course");

        assertThat(reviews.count()).isEqualTo(1);
    }

    @Test
    void rejectsRatingOutOfRange() {
        assertThatThrownBy(() -> reviewService.addReview(userId, courseId, 0, "Bad"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> reviewService.addReview(userId, courseId, 6, "Bad"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(reviews.count()).isZero();
    }

    @Test
    void deletingCourseRemovesReviewsButKeepsUser() {
        reviewService.addReview(userId, courseId, 4, "Good");

        courseService.deleteCourse(courseId);

        assertThat(reviews.count()).isZero();
        assertThat(users.count()).isEqualTo(1);
    }
}