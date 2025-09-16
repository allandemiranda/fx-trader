package br.allandemiranda.fx.service;

import br.allandemiranda.fx.dto.TickDto;
import br.allandemiranda.fx.mapper.TickMapper;
import br.allandemiranda.fx.model.Tick;
import br.allandemiranda.fx.repository.TickRepository;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@Transactional
@Getter(AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class TickService {

    private final TickRepository tickRepository;

    public TickDto add(TickDto tickDto) {
        Tick tick = TickMapper.toEntity(tickDto);
        Tick tickSaved = this.getTickRepository().save(tick);
        log.info("Tick saved: {}", tickSaved);
        return TickMapper.toDto(tickSaved);
    }

    public void remove(LocalDateTime dateTime) {
        log.info("Remove tick: {}", dateTime);
        this.getTickRepository().deleteById(dateTime);
    }

    @Transactional(readOnly = true)
    public TickDto getOldestTick() {
        log.info("Get oldest tick");
        return this.getTickRepository().findFirstByOrderByDateTimeAsc().map(TickMapper::toDto).orElseThrow();
    }
}
