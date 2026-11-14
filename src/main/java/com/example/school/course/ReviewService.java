package com.example.school.course;

import com.example.school.user.User;
import com.example.school.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
@Transactional(readOnly = true)
public class ReviewService {

    private final CourseReviewRepository reviews;
    private final CourseRepository courses;
    private final UserRepository users;

    public ReviewService(CourseReviewRepository reviews,
                         CourseRepository courses,
                         UserRepository users) {
        this.reviews = reviews;
        this.courses = courses;
        this.users = users;
    }

    @Transactional
    public Long addReview(Long userId, Long courseId, int rating, String comment) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5: " + rating);
        }
        User user = users.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User " + userId + " not found"));
        Course course = courses.findById(courseId)
                .orElseThrow(() -> new NoSuchElementException("Course " + courseId + " not found"));
        CourseReview review = new CourseReview(user, rating, comment);
        course.addReview(review);
        return reviews.save(review).getId();
    }
}