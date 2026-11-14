package com.example.school.course;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    boolean existsByUserIdAndAssignmentId(Long userId, Long assignmentId);
}