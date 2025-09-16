package br.allandemiranda.fx.controller;

import br.allandemiranda.fx.enums.AppliedPrice;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.nio.file.Path;

@RequestMapping("/api/garch")
public interface GarchController {
    @PostMapping
    ResponseEntity<Void> generateAllGarch(@RequestParam @Valid @NotNull Path candlestickFile, @RequestParam int sampleSize, @RequestParam @Valid @NotNull AppliedPrice appliedTo, @RequestParam double pipSize, @RequestParam int horizonBars, @RequestParam double alphaTP, @RequestParam double alphaSL);
}
