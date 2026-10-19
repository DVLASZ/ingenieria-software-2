package co.edu.unicauca.simulation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Mantiene un catálogo de preguntas publicadas y permite resolver simulacros. */
@SpringBootApplication
public class SimulationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SimulationServiceApplication.class, args);
    }
}
