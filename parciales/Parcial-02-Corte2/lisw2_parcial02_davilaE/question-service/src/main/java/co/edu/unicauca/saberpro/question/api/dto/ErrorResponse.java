package co.edu.unicauca.saberpro.question.api.dto;

import java.util.List;

public record ErrorResponse(String error, List<String> details) {
}
