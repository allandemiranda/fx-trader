package br.allandemiranda.fx.service;

import br.allandemiranda.fx.dto.CandlestickDto;
import br.allandemiranda.fx.mapper.CandlestickMapper;
import br.allandemiranda.fx.model.Candlestick;
import br.allandemiranda.fx.repository.CandlestickRepository;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.function.Consumer;
import java.util.stream.Stream;

@Slf4j
@Service
@Transactional
@Getter(AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class CandlestickService {

    private final CandlestickRepository candlestickRepository;

    public CandlestickDto add(CandlestickDto candlestickDto) {
        Candlestick candlestick = CandlestickMapper.toEntity(candlestickDto);
        Candlestick saved = this.getCandlestickRepository().save(candlestick);
        log.info("Candlestick saved: {}", saved);
        return CandlestickMapper.toDto(saved);
    }

    public void remove(LocalDateTime openDateTime) {
        log.info("Removing Candlestick from database for openDateTime: {}", openDateTime);
        this.getCandlestickRepository().deleteById(openDateTime);
    }

    @Transactional(readOnly = true)
    public CandlestickDto get(LocalDateTime openDateTime) {
        Candlestick candlestick = this.getCandlestickRepository().getReferenceById(openDateTime);
        log.info("Candlestick found: {}", candlestick);
        return CandlestickMapper.toDto(candlestick);
    }

    @Transactional(readOnly = true)
    public void processLastCandlesTicks(LocalDateTime pivot, long n, Consumer<CandlestickDto> consumer) {
        try (Stream<Candlestick> stream = this.getCandlestickRepository().streamLastNBeforeOrEqualAsc(pivot, n)) {
            stream.map(CandlestickMapper::toDto).forEach(consumer);
        }
    }
}
