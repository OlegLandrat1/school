package com.example.school.course;

import com.example.school.user.User;
import com.example.school.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.NoSuchElementException;

@Service
@Transactional(readOnly = true)
public class QuizService {

    private final QuizRepository quizzes;
    private final QuizSubmissionRepository submissions;
    private final UserRepository users;

    public QuizService(QuizRepository quizzes,
                       QuizSubmissionRepository submissions,
                       UserRepository users) {
        this.quizzes = quizzes;
        this.submissions = submissions;
        this.users = users;
    }

    @Transactional
    public Long recordAttempt(Long userId, Long quizId, int score) {
        if (score < 0) {
            throw new IllegalArgumentException("Score must not be negative: " + score);
        }
        User user = users.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User " + userId + " not found"));
        Quiz quiz = quizzes.findById(quizId)
                .orElseThrow(() -> new NoSuchElementException("Quiz " + quizId + " not found"));
        return submissions.save(new QuizSubmission(user, quiz, Instant.now(), score)).getId();
    }
}