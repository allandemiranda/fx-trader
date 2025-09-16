package br.allandemiranda.fx.service.impl;

import br.allandemiranda.fx.dto.MACDDto;
import br.allandemiranda.fx.mapper.MACDMapper;
import br.allandemiranda.fx.model.MACD;
import br.allandemiranda.fx.repository.MacdRepository;
import br.allandemiranda.fx.service.MacdService;
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
public class MacdServiceImpl implements MacdService {

    private final MacdRepository macdRepository;
    private final MACDMapper macdMapper;

    @Override
    public @NotNull MACDDto addMACD(@NotNull @Valid MACDDto macdDto) {
        log.debug("addMACD {}", macdDto);
        MACD macd = this.getMacdMapper().toEntity(macdDto);
        MACD saved = this.getMacdRepository().save(macd);
        return this.getMacdMapper().toDto(saved);
    }
}
