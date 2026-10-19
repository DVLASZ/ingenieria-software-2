package co.edu.unicauca.history.controller;

import co.edu.unicauca.history.dto.EntradaDto;
import co.edu.unicauca.history.service.HistoryService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/history")
public class HistoryController {

    private final HistoryService servicio;

    public HistoryController(HistoryService servicio) {
        this.servicio = servicio;
    }

    /** Línea de tiempo de una pregunta, de la más antigua a la más reciente. */
    @GetMapping("/{questionId}")
    public List<EntradaDto> lineaDeTiempo(@PathVariable Long questionId) {
        return servicio.lineaDeTiempo(questionId).stream().map(EntradaDto::de).toList();
    }

    /** Auditoría: todos los eventos (inicios de sesión incluidos), filtrables por tipo, del más reciente al más antiguo. */
    @GetMapping("/audit")
    public Map<String, Object> auditoria(@RequestParam(required = false) String tipo,
                                         @RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "20") int size) {
        Page<co.edu.unicauca.history.model.Entrada> pagina = servicio.auditoria(tipo, page, size);
        return Map.of("content", pagina.getContent().stream().map(EntradaDto::de).toList(),
                "page", pagina.getNumber(), "size", pagina.getSize(), "totalElements", pagina.getTotalElements());
    }
}
