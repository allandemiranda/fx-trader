package br.allandemiranda.fx.controller;

import br.allandemiranda.fx.enums.AppliedPrice;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.nio.file.Path;

@RequestMapping("/api/emas")
public interface EmasController {
    @PostMapping
    ResponseEntity<Void> generateAllEmas(@RequestParam @Valid @NotNull Path candlestickFile, @RequestParam @Min(2) int emaHighPeriod, @RequestParam @Min(2) int emaMidPeriod, @RequestParam @Min(2) int emaLowPeriod, @RequestParam @Valid @NotNull AppliedPrice appliedTo);
}
