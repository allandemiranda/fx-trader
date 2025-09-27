package br.allandemiranda.fx.service.impl;

import br.allandemiranda.fx.dto.EMAsDto;
import br.allandemiranda.fx.mapper.EMAsMapper;
import br.allandemiranda.fx.model.EMAs;
import br.allandemiranda.fx.repository.EmasRepository;
import br.allandemiranda.fx.service.EmasService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
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
public class EmasServiceImpl implements EmasService {

    private final EmasRepository emasRepository;
    private final EMAsMapper emasMapper;

    @Override
    public @NotNull EMAsDto add(@NotNull @Valid EMAsDto emasDto) {
        EMAs emas = this.getEmasMapper().toEntity(emasDto);
        EMAs saved = this.getEmasRepository().save(emas);
        return this.getEmasMapper().toDto(saved);
    }

    @Override
    public @NotNull Optional<EMAsDto> getEmas(@NotNull LocalDateTime timestamp) {
        return this.getEmasRepository().getEmasByTimestamp(timestamp).map(this.getEmasMapper()::toDto);
    }
}
