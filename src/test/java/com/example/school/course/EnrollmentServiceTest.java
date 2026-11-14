package com.example.school.course;

import com.example.school.user.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class EnrollmentServiceTest {

    @Autowired
    private EnrollmentService enrollmentService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private EnrollmentRepository enrollments;

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

        User student = users.save(new User("anna", "hash", Role.STUDENT));
        profiles.save(new Profile(student, "Anna Petrova", "Student"));
        userId = student.getId();

        courseId = courses.save(buildCourse("Java")).getId();
    }

    @Test
    void enrollsStudentOnce() {
        enrollmentService.enroll(userId, courseId);

        assertThat(enrollments.count()).isEqualTo(1);
    }

    @Test
    void rejectsSecondEnrollmentInService() {
        enrollmentService.enroll(userId, courseId);

        assertThatThrownBy(() -> enrollmentService.enroll(userId, courseId))
                .isInstanceOf(IllegalStateException.class);
        assertThat(enrollments.count()).isEqualTo(1);
    }

    @Test
    void databaseRejectsDuplicateEnrollment() {
        enrollmentService.enroll(userId, courseId);
        User user = users.findById(userId).orElseThrow();
        Course course = courses.findById(courseId).orElseThrow();

        assertThatThrownBy(() -> enrollments.saveAndFlush(
                new Enrollment(user, course, Instant.now())))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void deletingCourseRemovesEnrollmentsButKeepsUser() {
        enrollmentService.enroll(userId, courseId);

        courseService.deleteCourse(courseId);

        assertThat(enrollments.count()).isZero();
        assertThat(users.count()).isEqualTo(1);
    }

    private Course buildCourse(String title) {
        Course course = new Course(title);
        Module module = new Module("Intro", 1);
        course.addModule(module);

        Lesson lesson = new Lesson("Hello", "Text of the lesson", "https://example.com/video");
        module.addLesson(lesson);

        lesson.addAssignment(new Assignment("Homework 1", "Write hello world", LocalDate.of(2026, 12, 1), 10));
        return course;
    }
}