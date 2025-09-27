package br.allandemiranda.fx.service.impl;

import br.allandemiranda.fx.dto.CandlestickDto;
import br.allandemiranda.fx.service.CandlestickService;
import br.allandemiranda.fx.service.ValidateBean;
import br.allandemiranda.fx.utils.ReadFileDataUtils;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.function.Consumer;

@Slf4j
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
                    .filter(columns -> columns.length >= 6)
                    .map(columns -> new CandlestickFileLineModel(columns[0], columns[1], columns[2], columns[3], columns[4], columns[5]))
                    .map(candlestick -> {
                        LocalDateTime timestamp = ReadFileDataUtils.getDateTime(candlestick.openDate(), candlestick.openTime());
                        double open = ReadFileDataUtils.getPrice(candlestick.open());
                        double high = ReadFileDataUtils.getPrice(candlestick.high());
                        double low = ReadFileDataUtils.getPrice(candlestick.low());
                        double close = ReadFileDataUtils.getPrice(candlestick.close());
                        CandlestickDto candlestickDto = new CandlestickDto(timestamp, open, high, low, close);

                        this.getValidateBean().validate(candlestickDto, timestamp);
                        return candlestickDto;
                    }).forEachOrdered(consumer);
        }
    }
}
