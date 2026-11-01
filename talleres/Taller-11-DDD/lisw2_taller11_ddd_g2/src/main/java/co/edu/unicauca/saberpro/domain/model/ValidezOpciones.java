package co.edu.unicauca.saberpro.domain.model;

import co.edu.unicauca.saberpro.domain.exception.ReglaNegocioException;
import java.util.List;

/** Contexto de Validación: las reglas que deben cumplir las opciones de una pregunta de selección múltiple. */
public final class ValidezOpciones {

    public static final int OPCIONES_EXACTAS = 4;
    public static final int CLAVES_EXACTAS = 1;

    private ValidezOpciones() {
    }

    /** Se comprueba al agregar una opción: no pasan de cuatro y no se repiten. */
    public static void verificarNuevaOpcion(List<OpcionRespuesta> actuales, OpcionRespuesta nueva) {
        if (actuales.size() >= OPCIONES_EXACTAS) {
            throw new ReglaNegocioException("Una pregunta no puede tener más de " + OPCIONES_EXACTAS + " opciones.");
        }
        if (actuales.stream().anyMatch(nueva::mismoTextoQue)) {
            throw new ReglaNegocioException("Ya existe una opción con ese texto.");
        }
    }

    /** Se comprueba antes de enviar a revisión: cuatro opciones y una sola clave. */
    public static void verificarParaEnvio(List<OpcionRespuesta> opciones) {
        if (opciones.size() != OPCIONES_EXACTAS) {
            throw new ReglaNegocioException("Debe tener exactamente " + OPCIONES_EXACTAS + " opciones de respuesta.");
        }
        long claves = opciones.stream().filter(OpcionRespuesta::esCorrecta).count();
        if (claves != CLAVES_EXACTAS) {
            throw new ReglaNegocioException("Debe existir exactamente " + CLAVES_EXACTAS + " opción correcta.");
        }
    }
}
