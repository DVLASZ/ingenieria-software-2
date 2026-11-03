package co.edu.unicauca.saberpro.domain;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/** El dominio solo puede depender de Java: ningún import de Spring, JPA ni de cualquier otra librería. */
class DominioPuroTest {

    private static final Path FUENTES = Path.of("src", "main", "java");
    private static final String PAQUETE_DOMINIO = "co.edu.unicauca.saberpro.domain";

    private static List<Path> clases() throws IOException {
        try (Stream<Path> todo = Files.walk(FUENTES)) {
            return todo.filter(p -> p.toString().endsWith(".java")).toList();
        }
    }

    private static List<String> imports(Path clase) throws IOException {
        return Files.readAllLines(clase).stream()
                .map(String::trim)
                .filter(l -> l.startsWith("import "))
                .map(l -> l.replaceFirst("^import\\s+(static\\s+)?", "").replace(";", ""))
                .toList();
    }

    @Test
    void hayClasesDeDominioQueRevisar() throws IOException {
        assertTrue(clases().size() >= 10);
    }

    @Test
    void ningunaClaseImportaAlgoQueNoSeaJavaONuestroDominio() throws IOException {
        for (Path clase : clases()) {
            for (String importado : imports(clase)) {
                boolean permitido = importado.startsWith("java.") || importado.startsWith(PAQUETE_DOMINIO + ".");
                assertTrue(permitido, clase.getFileName() + " importa " + importado);
            }
        }
    }

    @Test
    void ningunaClaseUsaAnotacionesDeFrameworks() throws IOException {
        for (Path clase : clases()) {
            String texto = Files.readString(clase);
            for (String prohibido : List.of("@Entity", "@Table", "@Id", "@Column", "@Autowired", "@Component",
                    "@Service", "@Repository")) {
                assertFalse(texto.contains(prohibido), clase.getFileName() + " usa " + prohibido);
            }
        }
    }

    @Test
    void elDominioSoloTieneLosTresPaquetesDeLaGuia() throws IOException {
        for (Path clase : clases()) {
            String ruta = clase.toString().replace('\\', '/');
            boolean enSuPaquete = ruta.contains("/domain/model/") || ruta.contains("/domain/exception/")
                    || ruta.contains("/domain/repository/");
            assertTrue(enSuPaquete, ruta + " no está en model, exception ni repository");
        }
    }
}
