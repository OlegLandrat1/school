package com.example.school.course;

import com.example.school.user.User;
import com.example.school.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.NoSuchElementException;

@Service
@Transactional(readOnly = true)
public class SubmissionService {

    private final SubmissionRepository submissions;
    private final UserRepository users;
    private final AssignmentRepository assignments;

    public SubmissionService(SubmissionRepository submissions,
                             UserRepository users,
                             AssignmentRepository assignments) {
        this.submissions = submissions;
        this.users = users;
        this.assignments = assignments;
    }

    @Transactional
    public Long submit(Long userId, Long assignmentId, String content) {
        if (submissions.existsByUserIdAndAssignmentId(userId, assignmentId)) {
            throw new IllegalStateException(
                    "Assignment " + assignmentId + " already submitted by user " + userId);
        }
        User user = users.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User " + userId + " not found"));
        Assignment assignment = assignments.findById(assignmentId)
                .orElseThrow(() -> new NoSuchElementException("Assignment " + assignmentId + " not found"));
        return submissions.save(new Submission(user, assignment, content, Instant.now())).getId();
    }
}