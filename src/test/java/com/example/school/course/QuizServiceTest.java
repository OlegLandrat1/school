package com.example.school.course;

import com.example.school.user.ProfileRepository;
import com.example.school.user.Role;
import com.example.school.user.User;
import com.example.school.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class QuizServiceTest {

    @Autowired
    private QuizService quizService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private CourseRepository courses;

    @Autowired
    private ModuleRepository modules;

    @Autowired
    private QuizRepository quizzes;

    @Autowired
    private QuestionRepository questions;

    @Autowired
    private AnswerOptionRepository options;

    @Autowired
    private QuizSubmissionRepository submissions;

    @Autowired
    private UserRepository users;

    @Autowired
    private ProfileRepository profiles;

    private Long userId;
    private Long courseId;
    private Long quizId;

    @BeforeEach
    void setUp() {
        courses.deleteAll();
        profiles.deleteAll();
        users.deleteAll();

        userId = users.save(new User("anna", "hash", Role.STUDENT)).getId();

        courseId = courses.save(buildCourse()).getId();
        quizId = quizzes.findAll().get(0).getId();
    }

    @Test
    void savesQuizWithQuestionsAndOptions() {
        assertThat(quizzes.count()).isEqualTo(1);
        assertThat(questions.count()).isEqualTo(2);
        assertThat(options.count()).isEqualTo(4);
    }

    @Test
    void recordsAttemptForUser() {
        quizService.recordAttempt(userId, quizId, 7);

        assertThat(submissions.count()).isEqualTo(1);
    }

    @Test
    void rejectsNegativeScore() {
        assertThatThrownBy(() -> quizService.recordAttempt(userId, quizId, -1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(submissions.count()).isZero();
    }

    @Test
    void databaseRejectsSecondQuizForSameModule() {
        Module module = modules.findAll().get(0);
        Quiz second = new Quiz("Second", 10);
        second.attachTo(module);

        assertThatThrownBy(() -> quizzes.saveAndFlush(second))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThat(quizzes.count()).isEqualTo(1);
    }

    @Test
    void deletingCourseRemovesWholeQuizTreeButKeepsUser() {
        quizService.recordAttempt(userId, quizId, 5);

        courseService.deleteCourse(courseId);

        assertThat(quizzes.count()).isZero();
        assertThat(questions.count()).isZero();
        assertThat(options.count()).isZero();
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

        Quiz quiz = new Quiz("Check 1", 15);
        module.assignQuiz(quiz);

        Question first = new Question("What is 2 + 2?", false);
        first.addOption(new AnswerOption("3", false));
        first.addOption(new AnswerOption("4", true));
        quiz.addQuestion(first);

        Question second = new Question("Which types are primitive?", true);
        second.addOption(new AnswerOption("int", true));
        second.addOption(new AnswerOption("String", false));
        quiz.addQuestion(second);

        return course;
    }
}