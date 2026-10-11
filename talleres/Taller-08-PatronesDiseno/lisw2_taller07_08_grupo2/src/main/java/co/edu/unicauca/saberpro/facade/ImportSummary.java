package co.edu.unicauca.saberpro.facade;

import java.util.List;

/**
 * Resultado de importar preguntas desde el servicio externo.
 *
 * @param imported preguntas que quedaron registradas en el banco
 * @param rejected motivo de cada pregunta que no se pudo registrar
 */
public record ImportSummary(int imported, List<String> rejected) {

    public ImportSummary {
        rejected = List.copyOf(rejected);
    }
}
