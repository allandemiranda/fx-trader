package br.allandemiranda.fx.service;

import br.allandemiranda.fx.dto.CandlestickDto;
import br.allandemiranda.fx.dto.GarchDto;
import br.allandemiranda.fx.mapper.CandlestickMapper;
import br.allandemiranda.fx.mapper.GarchMapper;
import br.allandemiranda.fx.model.Candlestick;
import br.allandemiranda.fx.model.Garch;
import br.allandemiranda.fx.repository.GarchRepository;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@Transactional
@Getter(AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class GarchService {

    private final GarchRepository garchRepository;
    private final CandlestickService candlestickService;

    public GarchDto add(GarchDto garchDto) {
        Garch garch = GarchMapper.toEntity(garchDto);
        Garch saved = this.getGarchRepository().save(garch);
        log.info("Saved garch: {}", saved);
        return GarchMapper.toDto(saved);
    }

    public void delete(LocalDateTime timeStamp) {
        CandlestickDto candlestickDto = this.getCandlestickService().get(timeStamp);
        Candlestick candlestick = CandlestickMapper.toEntity(candlestickDto);
        log.info("Delete candlestick: {}", candlestick);
        this.getGarchRepository().deleteById(candlestick);
    }
}
