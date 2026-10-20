package co.edu.unicauca.tracking.dto;

import co.edu.unicauca.tracking.model.UserStats;

/** Contratos REST del seguimiento académico. */
public final class Dtos {

    private Dtos() {
    }

    public record UsuarioDto(Long userId, int simulacros, double promedio, int correctas, int preguntas, double acierto) {
        public static UsuarioDto de(UserStats s) {
            double acierto = s.getPreguntas() == 0 ? 0 : Math.round(1000.0 * s.getCorrectas() / s.getPreguntas()) / 10.0;
            return new UsuarioDto(s.getUserId(), s.getSimulacros(), s.promedio(), s.getCorrectas(), s.getPreguntas(), acierto);
        }
    }

    public record ResumenDto(int usuarios, int simulacros, double promedioGeneral) {
    }
}
