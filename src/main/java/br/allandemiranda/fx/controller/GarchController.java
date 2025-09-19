package br.allandemiranda.fx.controller;

import br.allandemiranda.fx.service.CandlestickService;
import br.allandemiranda.fx.service.GarchService;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.Executor;

@Slf4j
@RestController
@RequestMapping("/api/garchs")
@Getter(AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class GarchController {

    private final GarchService garchService;
    private final CandlestickService candlestickService;
    private final Executor executor;

    @GetMapping
    public ResponseEntity<Void> processData() {
//        this.getCandlestickService().processLastCandlesTicks();

        return ResponseEntity.ok().build();
    }

}
