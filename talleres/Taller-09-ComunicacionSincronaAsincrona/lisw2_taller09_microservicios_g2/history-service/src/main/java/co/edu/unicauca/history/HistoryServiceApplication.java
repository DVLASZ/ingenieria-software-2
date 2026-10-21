package co.edu.unicauca.history;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Guarda el historial de cada pregunta y la auditoría de accesos a partir de todos los eventos. */
@SpringBootApplication
public class HistoryServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(HistoryServiceApplication.class, args);
    }
}
