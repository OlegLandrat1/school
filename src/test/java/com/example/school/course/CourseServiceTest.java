package com.example.school.course;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class CourseServiceTest {

    @Autowired
    private CourseService service;

    @Autowired
    private CourseRepository courses;

    @BeforeEach
    void cleanUp() {
        courses.deleteAll();
    }

    @Test
    void loadsModulesOutsideTransaction() {
        Long id = service.createCourse("Java", List.of("Intro", "Collections"));

        Course course = service.getCourse(id);

        assertThat(course.getModules()).hasSize(2);
    }
}