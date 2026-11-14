package com.example.school.course;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "questions")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "question_text", nullable = false)
    private String text;

    @Column(name = "multiple_choice", nullable = false)
    private boolean multipleChoice;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<AnswerOption> answerOptions = new ArrayList<>();

    protected Question() {
    }

    public Question(String text, boolean multipleChoice) {
        this.text = text;
        this.multipleChoice = multipleChoice;
    }

    void attachTo(Quiz quiz) {
        this.quiz = quiz;
    }

    public void addOption(AnswerOption option) {
        option.attachTo(this);
        answerOptions.add(option);
    }

    public Long getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public boolean isMultipleChoice() {
        return multipleChoice;
    }

    public List<AnswerOption> getAnswerOptions() {
        return answerOptions;
    }
}