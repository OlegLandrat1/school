package com.example.school.course;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CourseCatalogTest {

    @Autowired
    private CourseService courseService;

    @Autowired
    private CourseRepository courses;

    @Autowired
    private CategoryRepository categories;

    @Autowired
    private TagRepository tags;

    @BeforeEach
    void setUp() {
        courses.deleteAll();
        categories.deleteAll();
        tags.deleteAll();
    }

    @Test
    void assignsCategoryAndTagsWithoutDuplicates() {
        Long courseId = courses.save(new Course("Java")).getId();
        Long categoryId = categories.save(new Category("Backend")).getId();
        Long javaId = tags.save(new Tag("java")).getId();
        Long springId = tags.save(new Tag("spring")).getId();

        courseService.assignCategory(courseId, categoryId);
        courseService.addTag(courseId, javaId);
        courseService.addTag(courseId, springId);
        courseService.addTag(courseId, javaId);

        Course course = courses.findById(courseId).orElseThrow();
        assertThat(course.getCategory().getName()).isEqualTo("Backend");
        assertThat(course.getTags()).hasSize(2);
    }

    @Test
    void deletingCourseKeepsCategoryAndTags() {
        Long courseId = courses.save(new Course("Java")).getId();
        Long categoryId = categories.save(new Category("Backend")).getId();
        Long tagId = tags.save(new Tag("java")).getId();
        courseService.assignCategory(courseId, categoryId);
        courseService.addTag(courseId, tagId);

        courseService.deleteCourse(courseId);

        assertThat(courses.count()).isZero();
        assertThat(categories.count()).isEqualTo(1);
        assertThat(tags.count()).isEqualTo(1);
    }

    @Test
    void rejectsDuplicateCategoryName() {
        categories.saveAndFlush(new Category("Backend"));

        assertThatThrownBy(() -> categories.saveAndFlush(new Category("Backend")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}