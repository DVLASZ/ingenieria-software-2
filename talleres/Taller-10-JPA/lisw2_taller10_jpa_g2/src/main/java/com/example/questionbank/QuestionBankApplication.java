package com.example.questionbank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point of the question bank application.
 */
@SpringBootApplication
public class QuestionBankApplication {

    /** Creates the application class; Spring Boot builds it when the application starts. */
    public QuestionBankApplication() {
    }

    /**
     * Starts the Spring Boot application.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(QuestionBankApplication.class, args);
    }
}
