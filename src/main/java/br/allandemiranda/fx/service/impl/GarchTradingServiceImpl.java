package br.allandemiranda.fx.service.impl;

import br.allandemiranda.fx.dto.GarchTradingDto;
import br.allandemiranda.fx.mapper.GarchTradingMapper;
import br.allandemiranda.fx.model.GarchTrading;
import br.allandemiranda.fx.repository.GarchTradingRepository;
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
public class GarchTradingServiceImpl implements br.allandemiranda.fx.service.GarchTradingService {

    private final GarchTradingRepository garchTradingRepository;
    private final GarchTradingMapper garchTradingMapper;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public @NotNull GarchTradingDto addGarchTrading(@NotNull @Valid GarchTradingDto garchTradingDto) {
        final GarchTrading garchTrading = this.getGarchTradingMapper().toEntity(garchTradingDto);
        final GarchTrading saved = this.getGarchTradingRepository().save(garchTrading);
        return this.getGarchTradingMapper().toDto(saved);
    }

    @Override
    @NotNull
    @Transactional(readOnly = true)
    public Optional<GarchTradingDto> getGarchTrading(@Valid @NotNull @PastOrPresent LocalDateTime timestamp) {
        return this.getGarchTradingRepository().getGarchByTimestamp(timestamp).map(this.getGarchTradingMapper()::toDto);
    }
}
