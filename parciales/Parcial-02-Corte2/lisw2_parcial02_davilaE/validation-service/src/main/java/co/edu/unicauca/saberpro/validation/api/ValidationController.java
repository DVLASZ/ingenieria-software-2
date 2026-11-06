package co.edu.unicauca.saberpro.validation.api;

import co.edu.unicauca.saberpro.validation.api.dto.ValidationRequest;
import co.edu.unicauca.saberpro.validation.api.dto.ValidationResponse;
import co.edu.unicauca.saberpro.validation.domain.JustificationRule;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ValidationController {

    private final JustificationRule rule = new JustificationRule();

    /** siempre responde 200; el cuerpo dice si es válida */
    @PostMapping("/validate")
    public ValidationResponse validate(@RequestBody ValidationRequest request) {
        if (rule.isValid(request.justification())) {
            return new ValidationResponse(true, "Justificación válida");
        }
        return new ValidationResponse(false,
                "La justificación debe tener más de " + JustificationRule.MIN_LENGTH + " caracteres");
    }
}
