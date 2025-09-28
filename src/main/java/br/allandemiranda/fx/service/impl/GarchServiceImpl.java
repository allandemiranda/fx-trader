package br.allandemiranda.fx.service.impl;

import br.allandemiranda.fx.dto.GarchDto;
import br.allandemiranda.fx.mapper.GarchMapper;
import br.allandemiranda.fx.model.Garch;
import br.allandemiranda.fx.repository.GarchRepository;
import br.allandemiranda.fx.service.GarchService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
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
public class GarchServiceImpl implements GarchService {

    private final GarchRepository garchRepository;
    private final GarchMapper garchMapper;

    @Override
    @NotNull
    public GarchDto addGarch(@NotNull @Valid GarchDto garchDto) {
        log.debug("addGarch {}", garchDto);
        Garch garch = this.getGarchMapper().toEntity(garchDto);
        Garch saved = this.getGarchRepository().save(garch);
        return this.getGarchMapper().toDto(saved);
    }

    @Override
    @NotNull
    @Transactional(readOnly = true)
    public Optional<GarchDto> getGarch(@Valid @NotNull @PastOrPresent LocalDateTime timestamp) {
        return this.getGarchRepository().getGarchByTimestamp(timestamp).map(this.getGarchMapper()::toDto);
    }
}
