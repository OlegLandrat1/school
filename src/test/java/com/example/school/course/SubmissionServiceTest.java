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
class SubmissionServiceTest {

    @Autowired
    private SubmissionService submissionService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private SubmissionRepository submissions;

    @Autowired
    private CourseRepository courses;

    @Autowired
    private AssignmentRepository assignments;

    @Autowired
    private UserRepository users;

    @Autowired
    private ProfileRepository profiles;

    private Long userId;
    private Long courseId;
    private Long assignmentId;

    @BeforeEach
    void setUp() {
        courses.deleteAll();
        profiles.deleteAll();
        users.deleteAll();

        User student = users.save(new User("anna", "hash", Role.STUDENT));
        profiles.save(new Profile(student, "Anna Petrova", "Student"));
        userId = student.getId();

        courseId = courses.save(buildCourse()).getId();
        assignmentId = assignments.findAll().get(0).getId();
    }

    @Test
    void submitsAssignmentOnce() {
        submissionService.submit(userId, assignmentId, "My answer");

        assertThat(submissions.count()).isEqualTo(1);
    }

    @Test
    void rejectsSecondSubmissionInService() {
        submissionService.submit(userId, assignmentId, "First");

        assertThatThrownBy(() -> submissionService.submit(userId, assignmentId, "Second"))
                .isInstanceOf(IllegalStateException.class);
        assertThat(submissions.count()).isEqualTo(1);
    }

    @Test
    void databaseRejectsDuplicateSubmission() {
        submissionService.submit(userId, assignmentId, "First");
        User user = users.findById(userId).orElseThrow();
        Assignment assignment = assignments.findById(assignmentId).orElseThrow();

        assertThatThrownBy(() -> submissions.saveAndFlush(
                new Submission(user, assignment, "Second", Instant.now())))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void deletingCourseRemovesSubmissionsButKeepsUser() {
        submissionService.submit(userId, assignmentId, "Answer");

        courseService.deleteCourse(courseId);

        assertThat(submissions.count()).isZero();
        assertThat(users.count()).isEqualTo(1);
    }

    private Course buildCourse() {
        Course course = new Course("Java");
        Module module = new Module("Intro", 1);
        course.addModule(module);

        Lesson lesson = new Lesson("Hello", "Text of the lesson", "https://example.com/video");
        module.addLesson(lesson);

        lesson.addAssignment(new Assignment("Homework 1", "Write hello world", LocalDate.of(2026, 12, 1), 10));
        return course;
    }
}