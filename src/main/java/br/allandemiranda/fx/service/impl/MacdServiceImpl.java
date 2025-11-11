package br.allandemiranda.fx.service.impl;

import br.allandemiranda.fx.dto.MACDDto;
import br.allandemiranda.fx.mapper.MACDMapper;
import br.allandemiranda.fx.model.MACD;
import br.allandemiranda.fx.repository.MacdRepository;
import br.allandemiranda.fx.service.MacdService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Validated
@Transactional
@Getter(AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class MacdServiceImpl implements MacdService {

    private final MacdRepository macdRepository;
    private final MACDMapper macdMapper;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public @NotNull MACDDto addMACD(@NotNull @Valid MACDDto macdDto) {
        final MACD macd = this.getMacdMapper().toEntity(macdDto);
        final MACD saved = this.getMacdRepository().save(macd);
        return this.getMacdMapper().toDto(saved);
    }

    @Override
    @NotNull
    @Transactional(readOnly = true)
    public Optional<MACDDto> getMACD(@Valid @NotNull @PastOrPresent LocalDateTime timestamp) {
        return this.getMacdRepository().getMACDByTimestamp(timestamp).map(this.getMacdMapper()::toDto);
    }
}
