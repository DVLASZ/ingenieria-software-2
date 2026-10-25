package com.example.questionbank.domain.model;

/**
 * Roles a user can have in the question bank.
 */
public enum Role {
    /** Manages users, roles and system parameters. */
    ADMIN,
    /** Creates and edits questions. */
    AUTHOR,
    /** Evaluates the quality of questions. */
    REVIEWER,
    /** Organizes quizzes. */
    TEACHER,
    /** Answers questions and reads results. */
    STUDENT
}
