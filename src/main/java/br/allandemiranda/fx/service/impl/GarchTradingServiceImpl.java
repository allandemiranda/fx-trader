package br.allandemiranda.fx.service.impl;

import br.allandemiranda.fx.dto.GarchTradingDto;
import br.allandemiranda.fx.mapper.GarchTradingMapper;
import br.allandemiranda.fx.model.GarchTrading;
import br.allandemiranda.fx.repository.GarchTradingRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Slf4j
@Service
@Validated
@Transactional
@Getter(AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class GarchTradingServiceImpl implements br.allandemiranda.fx.service.GarchTradingService {

    private final GarchTradingRepository garchTradingRepository;
    private final GarchTradingMapper garchTradingMapper;

    @Override
    public @NotNull GarchTradingDto addGarchTrading(@NotNull @Valid GarchTradingDto garchTradingDto) {
        GarchTrading garchTrading = this.getGarchTradingMapper().toEntity(garchTradingDto);
        GarchTrading saved = this.getGarchTradingRepository().save(garchTrading);
        return this.getGarchTradingMapper().toDto(saved);
    }
}
