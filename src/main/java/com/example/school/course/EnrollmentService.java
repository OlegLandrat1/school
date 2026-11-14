package com.example.school.course;

import com.example.school.user.User;
import com.example.school.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.NoSuchElementException;

@Service
@Transactional(readOnly = true)
public class EnrollmentService {

    private final EnrollmentRepository enrollments;
    private final UserRepository users;
    private final CourseRepository courses;

    public EnrollmentService(EnrollmentRepository enrollments,
                             UserRepository users,
                             CourseRepository courses) {
        this.enrollments = enrollments;
        this.users = users;
        this.courses = courses;
    }

    @Transactional
    public Long enroll(Long userId, Long courseId) {
        if (enrollments.existsByUserIdAndCourseId(userId, courseId)) {
            throw new IllegalStateException(
                    "User " + userId + " already enrolled in course " + courseId);
        }
        User user = users.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User " + userId + " not found"));
        Course course = courses.findById(courseId)
                .orElseThrow(() -> new NoSuchElementException("Course " + courseId + " not found"));
        return enrollments.save(new Enrollment(user, course, Instant.now())).getId();
    }
}