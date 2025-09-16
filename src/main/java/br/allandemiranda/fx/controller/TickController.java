package br.allandemiranda.fx.controller;

import br.allandemiranda.fx.service.TickService;
import br.allandemiranda.fx.utils.ReadFileDataUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Path;
import java.util.concurrent.Executor;

@Slf4j
@RestController
@RequestMapping("/api/ticks")
@Getter(AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class TickController {

    private final TickService tickService;
    private final Executor executor;

    @GetMapping
    public ResponseEntity<Void> readFileData() {
        Path tickFile = Path.of("C:\\Users\\allan\\OneDrive\\Documentos\\MT5\\EURUSD_201112190000_202509122358.csv");
        log.info("Reading ticks file: {}", tickFile);
        this.getExecutor().execute(() -> ReadFileDataUtils.readTicksFile(tickFile, dto -> this.getTickService().add(dto)));
        log.info("Reading ticks file done");
        return ResponseEntity.ok().build();
    }

}
