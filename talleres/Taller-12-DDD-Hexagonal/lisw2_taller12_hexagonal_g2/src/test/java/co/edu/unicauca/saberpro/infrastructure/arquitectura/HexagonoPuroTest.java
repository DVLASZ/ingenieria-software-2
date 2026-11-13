package co.edu.unicauca.saberpro.infrastructure.arquitectura;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/** Revisa los imports de los archivos: ni el dominio ni la aplicación pueden conocer frameworks o infraestructura. */
class HexagonoPuroTest {

    static final Path BASE = Path.of("src/main/java/co/edu/unicauca/saberpro");

    static List<String> imports(String paquete) throws IOException {
        try (Stream<Path> archivos = Files.walk(BASE.resolve(paquete))) {
            return archivos.filter(a -> a.toString().endsWith(".java")).flatMap(a -> {
                try {
                    return Files.readAllLines(a).stream().filter(l -> l.startsWith("import "))
                            .map(l -> a.getFileName() + ": " + l);
                } catch (IOException e) {
                    throw new IllegalStateException(e);
                }
            }).toList();
        }
    }

    @Test
    void elDominioNoImportaFrameworksNiInfraestructura() throws IOException {
        List<String> prohibidos = imports("domain").stream()
                .filter(l -> l.contains("org.springframework") || l.contains("jakarta.") || l.contains("javax.")
                        || l.contains("org.hibernate") || l.contains("com.fasterxml") || l.contains("org.slf4j")
                        || l.contains(".infrastructure.") || l.contains(".application."))
                .toList();
        assertEquals(List.of(), prohibidos);
    }

    @Test
    void laAplicacionSoloDependeDelDominio() throws IOException {
        List<String> prohibidos = imports("application").stream()
                .filter(l -> l.contains("org.springframework") || l.contains("jakarta.") || l.contains(".infrastructure."))
                .toList();
        assertEquals(List.of(), prohibidos);
    }

    @Test
    void elDominioSoloUsaClasesDelJdkYSuPropioPaquete() throws IOException {
        List<String> ajenos = imports("domain").stream()
                .filter(l -> !l.contains("import java.") && !l.contains("import co.edu.unicauca.saberpro.domain."))
                .toList();
        assertEquals(List.of(), ajenos);
    }

    @Test
    void existenLosPaquetesDeLaEstructuraHexagonal() {
        assertFalse(Files.notExists(BASE.resolve("domain/ports/inbound")));
        assertFalse(Files.notExists(BASE.resolve("domain/ports/outbound")));
        assertFalse(Files.notExists(BASE.resolve("application")));
        assertFalse(Files.notExists(BASE.resolve("infrastructure")));
    }
}
