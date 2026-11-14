package com.example.school.course;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional(readOnly = true)
public class CourseService {

    private final CourseRepository courses;
    private final CategoryRepository categories;
    private final TagRepository tags;

    public CourseService(CourseRepository courses,
                         CategoryRepository categories,
                         TagRepository tags) {
        this.courses = courses;
        this.categories = categories;
        this.tags = tags;
    }

    @Transactional
    public Long createCourse(String title, List<String> moduleTitles) {
        Course course = new Course(title);
        int index = 1;
        for (String moduleTitle : moduleTitles) {
            course.addModule(new Module(moduleTitle, index++));
        }
        return courses.save(course).getId();
    }

    public Course getCourse(Long id) {
        return courses.findWithModulesById(id)
                .orElseThrow(() -> new NoSuchElementException("Course " + id + " not found"));
    }

    @Transactional
    public void deleteCourse(Long id) {
        if (!courses.existsById(id)) {
            throw new NoSuchElementException("Course " + id + " not found");
        }
        courses.deleteById(id);
    }

    @Transactional
    public void assignCategory(Long courseId, Long categoryId) {
        Course course = findCourse(courseId);
        Category category = categories.findById(categoryId)
                .orElseThrow(() -> new NoSuchElementException("Category " + categoryId + " not found"));
        course.assignCategory(category);
    }

    @Transactional
    public void addTag(Long courseId, Long tagId) {
        Course course = findCourse(courseId);
        Tag tag = tags.findById(tagId)
                .orElseThrow(() -> new NoSuchElementException("Tag " + tagId + " not found"));
        course.addTag(tag);
    }

    private Course findCourse(Long id) {
        return courses.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Course " + id + " not found"));
    }
}