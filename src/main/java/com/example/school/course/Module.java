package com.example.school.course;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "modules")
public class Module {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(name = "order_index", nullable = false)
    private int orderIndex;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @OneToMany(mappedBy = "module", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Lesson> lessons = new ArrayList<>();

    @OneToOne(mappedBy = "module", cascade = CascadeType.ALL, orphanRemoval = true)
    private Quiz quiz;

    protected Module() {
    }

    public Module(String title, int orderIndex) {
        this.title = title;
        this.orderIndex = orderIndex;
    }

    void attachTo(Course course) {
        this.course = course;
    }

    public void addLesson(Lesson lesson) {
        lesson.attachTo(this);
        lessons.add(lesson);
    }

    public void assignQuiz(Quiz quiz) {
        quiz.attachTo(this);
        this.quiz = quiz;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public int getOrderIndex() {
        return orderIndex;
    }

    public List<Lesson> getLessons() {
        return lessons;
    }

    public Quiz getQuiz() {
        return quiz;
    }
}