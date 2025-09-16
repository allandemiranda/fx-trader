package br.allandemiranda.fx.controller;

import br.allandemiranda.fx.enums.Timeframe;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.nio.file.Path;
import java.time.DayOfWeek;

@RequestMapping("/api/garchTrading")
public interface GarchTradingController {
    @PostMapping
    ResponseEntity<Void> generateAllGarchTrading(@RequestParam @Valid @NotNull Path ticksFile, @RequestParam @Valid @NotNull Timeframe timeframe, @RequestParam @Valid @NotNull DayOfWeek tripleDay, @RequestParam double swapLongPts, @RequestParam double swapShortPts);
}
