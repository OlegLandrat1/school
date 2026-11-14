package com.example.school.course;

import com.example.school.user.User;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "quiz_submissions")
public class QuizSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "taken_at", nullable = false)
    private Instant takenAt;

    @Column(nullable = false)
    private int score;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    protected QuizSubmission() {
    }

    public QuizSubmission(User user, Quiz quiz, Instant takenAt, int score) {
        this.user = user;
        this.quiz = quiz;
        this.takenAt = takenAt;
        this.score = score;
    }

    public Long getId() {
        return id;
    }

    public Instant getTakenAt() {
        return takenAt;
    }

    public int getScore() {
        return score;
    }

    public User getUser() {
        return user;
    }

    public Quiz getQuiz() {
        return quiz;
    }
}