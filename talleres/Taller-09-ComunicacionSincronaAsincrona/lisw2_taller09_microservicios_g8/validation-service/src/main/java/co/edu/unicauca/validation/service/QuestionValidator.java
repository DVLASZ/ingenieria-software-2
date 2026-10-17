package co.edu.unicauca.validation.service;

import co.edu.unicauca.validation.dto.Dtos.ValidationRequest;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Reglas de validación estructural del proyecto: contexto presente, una única pregunta directa, exactamente cuatro
 * opciones razonables y distintas, una única respuesta correcta y sin «todas/ninguna de las anteriores».
 */
@Component
public class QuestionValidator {

    static final int LONGITUD_MAXIMA_OPCION = 300;
    private static final Set<String> CLAVES = Set.of("A", "B", "C", "D");

    public List<String> validar(ValidationRequest q) {
        List<String> errores = new ArrayList<>();
        if (esVacio(q.contexto())) {
            errores.add("El contexto es obligatorio");
        }
        if (esVacio(q.pregunta())) {
            errores.add("La pregunta directa es obligatoria");
        } else if (q.pregunta().chars().filter(c -> c == '?').count() != 1) {
            errores.add("Debe haber una única pregunta directa (exactamente un signo de interrogación)");
        }
        validarOpciones(q.opciones(), errores);
        if (q.respuestaCorrecta() == null || !CLAVES.contains(q.respuestaCorrecta().trim().toUpperCase(Locale.ROOT))) {
            errores.add("Debe indicarse una única respuesta correcta entre A, B, C y D");
        }
        return errores;
    }

    private void validarOpciones(List<String> opciones, List<String> errores) {
        if (opciones == null || opciones.size() != 4) {
            errores.add("Deben ser exactamente cuatro opciones");
            return;
        }
        Set<String> vistas = new HashSet<>();
        for (int i = 0; i < opciones.size(); i++) {
            String texto = opciones.get(i);
            String letra = String.valueOf((char) ('A' + i));
            if (esVacio(texto)) {
                errores.add("La opción " + letra + " no puede estar vacía");
                continue;
            }
            if (texto.length() > LONGITUD_MAXIMA_OPCION) {
                errores.add("La opción " + letra + " supera los " + LONGITUD_MAXIMA_OPCION + " caracteres");
            }
            String normal = normalizar(texto);
            if (normal.contains("todas las anteriores") || normal.contains("ninguna de las anteriores")) {
                errores.add("La opción " + letra + " no puede ser «todas» ni «ninguna de las anteriores»");
            }
            if (!vistas.add(normal)) {
                errores.add("La opción " + letra + " está repetida");
            }
        }
    }

    private static boolean esVacio(String s) {
        return s == null || s.isBlank();
    }

    private static String normalizar(String s) {
        String sinTildes = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return sinTildes.toLowerCase(Locale.ROOT).trim().replaceAll("\\s+", " ");
    }
}
