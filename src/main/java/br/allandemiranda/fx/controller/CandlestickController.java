package br.allandemiranda.fx.controller;

import br.allandemiranda.fx.service.CandlestickService;
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
@RequestMapping("/api/candlesticks")
@Getter(AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class CandlestickController {

    private final CandlestickService candlestickService;
    private final Executor executor;

    @GetMapping
    public ResponseEntity<Void> readFileData() {
        Path candlestickFile = Path.of("C:\\Users\\allan\\OneDrive\\Documentos\\MT5\\EURUSD_H1_200001030000_202509122300.csv");
        log.info("Reading candlesticks file: {}", candlestickFile);
        this.getExecutor().execute(() -> ReadFileDataUtils.readCandlestickFile(candlestickFile, dto -> this.getCandlestickService().add(dto)));
        log.info("Reading candlesticks file done");
        return ResponseEntity.ok().build();
    }
}
