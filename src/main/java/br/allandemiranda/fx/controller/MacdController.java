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

@RequestMapping("/api/macd")
public interface MacdController {
    @PostMapping
    ResponseEntity<Void> generateAllMacd(@RequestParam @Valid @NotNull Path candlesticksFile, @RequestParam @Valid @Min(2) int fastEmaPeriod, @RequestParam @Valid @Min(2) int slowEmaPeriod, @RequestParam @Valid @Min(2) int macdEmaPeriod, @RequestParam @Valid @NotNull AppliedPrice appliedTo);
}
