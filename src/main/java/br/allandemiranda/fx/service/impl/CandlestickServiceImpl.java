package br.allandemiranda.fx.service.impl;

import br.allandemiranda.fx.dto.CandlestickDto;
import br.allandemiranda.fx.service.CandlestickService;
import br.allandemiranda.fx.service.ValidateBean;
import br.allandemiranda.fx.utils.MarketSessionUtils;
import br.allandemiranda.fx.utils.ReadFileDataUtils;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.function.Consumer;

@Service
@Validated
@Transactional(readOnly = true)
@Getter(AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class CandlestickServiceImpl implements CandlestickService {

    private final ValidateBean validateBean;

    @SneakyThrows
    @Override
    public void consumerCandlesticks(@Valid @NotNull Path candlestickFile, @Valid @NotNull Consumer<CandlestickDto> consumer) {
        record CandlestickFileLineModel(String openDate, String openTime, String open, String high, String low, String close) {
        }
        try (BufferedReader br = Files.newBufferedReader(candlestickFile, StandardCharsets.UTF_8)) {
            br.lines()
                    .skip(1)
                    .filter(line -> !line.isBlank())
                    .map(line -> line.split("\t"))
                    .map(columns -> new CandlestickFileLineModel(columns[0], columns[1], columns[2], columns[3], columns[4], columns[5]))
                    .map(candlestick -> {
                        final ZonedDateTime timestamp = ReadFileDataUtils.getDateTime(candlestick.openDate(), candlestick.openTime(), MarketSessionUtils.MT5_SERVER_ZONE);
                        final double open = ReadFileDataUtils.getPrice(candlestick.open());
                        final double high = ReadFileDataUtils.getPrice(candlestick.high());
                        final double low = ReadFileDataUtils.getPrice(candlestick.low());
                        final double close = ReadFileDataUtils.getPrice(candlestick.close());
                        final CandlestickDto candlestickDto = new CandlestickDto(timestamp.toLocalDateTime(), open, high, low, close);

                        this.getValidateBean().validate(candlestickDto, timestamp.toLocalDateTime());
                        return candlestickDto;
                    }).forEachOrdered(consumer);
        }
    }
}
