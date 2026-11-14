package com.example.school.course;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quizzes")
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(name = "time_limit_minutes")
    private Integer timeLimitMinutes;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "module_id", nullable = false, unique = true)
    private Module module;

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Question> questions = new ArrayList<>();

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<QuizSubmission> submissions = new ArrayList<>();

    protected Quiz() {
    }

    public Quiz(String title, Integer timeLimitMinutes) {
        this.title = title;
        this.timeLimitMinutes = timeLimitMinutes;
    }

    void attachTo(Module module) {
        this.module = module;
    }

    public void addQuestion(Question question) {
        question.attachTo(this);
        questions.add(question);
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public Integer getTimeLimitMinutes() {
        return timeLimitMinutes;
    }

    public Module getModule() {
        return module;
    }

    public List<Question> getQuestions() {
        return questions;
    }
}