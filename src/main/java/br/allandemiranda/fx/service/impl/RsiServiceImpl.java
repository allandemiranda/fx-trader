package br.allandemiranda.fx.service.impl;

import br.allandemiranda.fx.dto.RSIDto;
import br.allandemiranda.fx.mapper.RSIMapper;
import br.allandemiranda.fx.model.RSI;
import br.allandemiranda.fx.repository.RsiRepository;
import br.allandemiranda.fx.service.RsiService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@Validated
@Transactional
@Getter(AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class RsiServiceImpl implements RsiService {

    private final RsiRepository rsiRepository;
    private final RSIMapper rsiMapper;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public @NotNull RSIDto addRSI(@NotNull @Valid RSIDto rsiDto) {
        RSI rsi = this.getRsiMapper().toEntity(rsiDto);
        RSI saved = this.getRsiRepository().save(rsi);
        return this.getRsiMapper().toDto(saved);
    }

    @Override
    @NotNull
    @Transactional(readOnly = true)
    public Optional<RSIDto> getRSI(@Valid @NotNull @PastOrPresent LocalDateTime timestamp) {
        return this.getRsiRepository().getRSIByTimestamp(timestamp).map(this.getRsiMapper()::toDto);
    }
}
