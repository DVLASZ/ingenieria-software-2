package co.edu.unicauca.saberpro.domain.model;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Contexto de Validación: expresiones que una pregunta no puede usar ("todas las anteriores", "ninguna de las
 * anteriores"). Ignora mayúsculas y tildes.
 */
public final class FiltroExpresion {

    private static final List<Pattern> PROHIBIDAS = List.of(
            Pattern.compile("\\btodas\\s+las\\s+(anteriores|opciones)\\b"),
            Pattern.compile("\\bninguna\\s+de\\s+las\\s+(anteriores|opciones)\\b"));

    private FiltroExpresion() {
    }

    public static boolean contieneExpresionProhibida(String texto) {
        if (texto == null) {
            return false;
        }
        String normalizado = normalizar(texto);
        return PROHIBIDAS.stream().anyMatch(p -> p.matcher(normalizado).find());
    }

    /** Minúsculas, sin tildes y con los espacios repetidos reducidos a uno. */
    static String normalizar(String texto) {
        String sinTildes = Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return sinTildes.toLowerCase(Locale.ROOT).trim().replaceAll("\\s+", " ");
    }
}
