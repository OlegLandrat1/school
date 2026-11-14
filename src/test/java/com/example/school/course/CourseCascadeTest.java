package com.example.school.course;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class CourseCascadeTest {

    @Autowired
    private CourseService service;

    @Autowired
    private CourseRepository courses;

    @Autowired
    private ModuleRepository modules;

    @Autowired
    private LessonRepository lessons;

    @Autowired
    private AssignmentRepository assignments;

    @BeforeEach
    void cleanUp() {
        courses.deleteAll();
    }

    @Test
    void savesWholeTree() {
        Long id = courses.save(buildCourse("Java")).getId();

        assertThat(courses.findById(id)).isPresent();
        assertThat(modules.count()).isEqualTo(1);
        assertThat(lessons.count()).isEqualTo(1);
        assertThat(assignments.count()).isEqualTo(1);
    }

    @Test
    void deletingCourseRemovesModulesLessonsAndAssignments() {
        Long id = courses.save(buildCourse("Java")).getId();

        service.deleteCourse(id);

        assertThat(courses.count()).isZero();
        assertThat(modules.count()).isZero();
        assertThat(lessons.count()).isZero();
        assertThat(assignments.count()).isZero();
    }

    @Test
    void deletingOneCourseKeepsAnotherIntact() {
        Long firstId = courses.save(buildCourse("A")).getId();
        courses.save(buildCourse("B"));

        service.deleteCourse(firstId);

        assertThat(courses.count()).isEqualTo(1);
        assertThat(courses.findAll().get(0).getTitle()).isEqualTo("B");
        assertThat(modules.count()).isEqualTo(1);
        assertThat(lessons.count()).isEqualTo(1);
        assertThat(assignments.count()).isEqualTo(1);
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